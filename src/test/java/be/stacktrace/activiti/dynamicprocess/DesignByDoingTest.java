package be.stacktrace.activiti.dynamicprocess;

import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.cmd.SetProcessDefinitionVersionCmd;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.util.ProcessDefinitionUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * 在节点中插入新节点
 *
 * This example shows how to implement design by doing with adaptive process definitions
 */
public class DesignByDoingTest {

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    private int counter = 0;

    @Test
    public void testSequentialUserTasksProcessModel() throws Exception {
        //创建流程
        designProcessByDoing_processModel();
//        //判断是否有未结束的instance 暂时屏蔽
//        assertThatNextProcessInstanceIsTheSame();
    }


    /**
     * 判断是否有未结束的instance
     */
    private void assertThatNextProcessInstanceIsTheSame() {
        // execute next process instance according to already created process definition
        String doingByDesignProcessInstanceId = activitiRule.getRuntimeService()
                .startProcessInstanceByKey("designByDoing-process").getId();

        assertAndCompleteTask("Make a record", doingByDesignProcessInstanceId, "Fred");

        assertAndCompleteTask("Create inventory list", doingByDesignProcessInstanceId, "John");

        assertAndCompleteTask("Collect inventory", doingByDesignProcessInstanceId, "Bill");

        assertThat("All process instances must be finished", this.activitiRule.getRuntimeService().createProcessInstanceQuery().count(), is(0L));
    }


    /**
     * 创建流程
     * @throws IOException
     */
    private void designProcessByDoing_processModel() throws IOException {
        //Fred通过执行流程来初始化设计，用一个任务创建第一个流程模型
        //创建一个单个节点的流程
        ProcessInstance processInstance = createAdaptiveProcessInstance("Fred", "Make a record");

//        assertThat("User task with the name 'Make a record' assigned to 'Fred' exists",
//                this.activitiRule.getTaskService().createTaskQuery().taskAssignee("Fred").taskName("Make a record").count(), is(1L));

        //生成bpmn文件
        exportProcessDefinition(processInstance.getProcessDefinitionId(), "target/step-1-", "designByDoing-model.bpmn");

        //Fred指定下一个节点
        processInstance = addUserTask(processInstance, "Create inventory list", "John", "target/step-2-");

        //现在Fred可以完成他的任务，流程实例可以继续下一步
        assertAndCompleteTask("Make a record", processInstance.getId(), "Fred");

        //断言下一个任务存在
        assertThat("User task with the name 'Create inventory list' assigned to 'John' exists",
                this.activitiRule.getTaskService().createTaskQuery().taskAssignee("John").taskName("Create inventory list").count(), is(1L));

        //流程实例仍未完成John必须指定谁必须负责接下来的流程执行
        processInstance = addUserTask(processInstance, "Collect inventory", "Bill", "target/step-3-");

        //现在John可以完成他的任务，流程实例可以继续下一步
        assertAndCompleteTask("Create inventory list", processInstance.getId(), "John");

        //所有节点到Bill这结束， Bill结束这个流程实例。流程节点添加完毕
        assertAndCompleteTask("Collect inventory", processInstance.getId(), "Bill");
    }


    /**
     * 增加节点
     * @param processInstance
     * @param userTaskName
     * @param assigneeId
     * @param namePrefix
     * @return
     * @throws IOException
     */
    private ProcessInstance addUserTask(ProcessInstance processInstance, String userTaskName, String assigneeId, String namePrefix) throws IOException {
        addUserTaskToProcessInstance(processInstance, userTaskName, assigneeId);
        processInstance = this.activitiRule.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstance.getId()).singleResult();

        //再生成一个bpmn文件
        exportProcessDefinition(processInstance.getProcessDefinitionId(), namePrefix, "designByDoing-model.bpmn");
        return processInstance;
    }


    /**
     * 完成节点任务
     * @param taskName
     * @param processInstanceId
     * @param assigneeId
     */
    private void assertAndCompleteTask(String taskName, String processInstanceId, String assigneeId) {
        Task task = this.activitiRule.getTaskService().createTaskQuery().
                processInstanceId(processInstanceId).
                taskName(taskName).
                taskAssignee(assigneeId).singleResult();
        assertThat(task, is(notNullValue()));
        this.activitiRule.getTaskService().complete(task.getId());
    }


    /**
     * 增加节点
     * @param processInstance
     * @param userTaskName
     * @param assignee
     */
    private void addUserTaskToProcessInstance(ProcessInstance processInstance, String userTaskName, String assignee) {
        ProcessEngineConfigurationImpl oldProcessEngineConfiguration = Context.getProcessEngineConfiguration();
        Context.setProcessEngineConfiguration((ProcessEngineConfigurationImpl) this.activitiRule.getProcessEngine().getProcessEngineConfiguration());
        //拿到原来的流程数据
        Process process = ProcessDefinitionUtil.getProcess(processInstance.getProcessDefinitionId());
        //增加新的节点数据
        process.addFlowElement(createUserTask("userTask"+this.counter, userTaskName, assignee));
        process.addFlowElement(createSequenceFlow("toUserTask" + this.counter, "userTask" + (this.counter-1), "userTask" + this.counter));
        process.removeFlowElement("toEnd");
        process.addFlowElement(createSequenceFlow("toEnd", "userTask" + this.counter, "end"));
        this.counter++;

        //部署
        deployModelWithProcess(process);
        //设置新流程的（新）版本
        upgradeProcessInstanceDefinitionVersion(processInstance);
        Context.setProcessEngineConfiguration(oldProcessEngineConfiguration);
    }


    /**
     * 设置新流程的（新）版本
     * @param processInstance
     */
    private void upgradeProcessInstanceDefinitionVersion(ProcessInstance processInstance) {
        ProcessDefinition processDefinition = this.activitiRule.getRepositoryService().getProcessDefinition(processInstance.getProcessDefinitionId());
        this.activitiRule.getManagementService().executeCommand(new SetProcessDefinitionVersionCmd(processInstance.getId(), processDefinition.getVersion() + 1));
    }


    /**
     * 部署
     * @param process
     * @return
     */
    private Deployment deployModelWithProcess(Process process) {
        BpmnModel model = new BpmnModel();
        model.addProcess(process);
        return activitiRule.getRepositoryService().createDeployment()
                .addBpmnModel("designByDoing-model.bpmn", model).name("DesignByDoing process deployment")
                .deploy();
    }


    /**
     * 创建一个单个节点的流程
     * @param currentUserId
     * @param taskName
     * @return
     */
    private ProcessInstance createAdaptiveProcessInstance(String currentUserId, String taskName) {
        //1、建立基本模型
        Process process = new Process();
        process.setId("designByDoing-process");

        //开始
        process.addFlowElement(createStartEvent());
        //连线
        process.addFlowElement(createSequenceFlow("toUserTask" + this.counter, "start", "userTask" + this.counter));
        //节点
        process.addFlowElement(createUserTask("userTask" + this.counter, taskName, currentUserId));
        //连线
        process.addFlowElement(createSequenceFlow("toEnd", "userTask"+this.counter, "end"));
        //结束
        process.addFlowElement(createEndEvent());
        this.counter++;

        //2、部署基本流程模型
        deployModelWithProcess(process);

        //3、根据流程定义启动流程实例
        return activitiRule.getRuntimeService().startProcessInstanceByKey("designByDoing-process");
    }


    /**
     * 生成bpmn文件
     * @param processDefinitionId
     * @param namePrefix
     * @param resourceName
     * @throws IOException
     */
    private void exportProcessDefinition(String processDefinitionId, String namePrefix, String resourceName) throws IOException {

        ProcessDefinition processDefinition = this.activitiRule.getRepositoryService().getProcessDefinition(processDefinitionId);
        //生成并保存bpmn文件
        InputStream processBpmn = activitiRule.getRepositoryService().getResourceAsStream(processDefinition.getDeploymentId(), resourceName);
        FileUtils.copyInputStreamToFile(processBpmn,new File(namePrefix + "process.bpmn20.xml"));

//        //生成并保存流程图片
//        InputStream processDiagram = activitiRule.getRepositoryService().getProcessDiagram(processDefinitionId);
//        FileUtils.copyInputStreamToFile(processDiagram, new File(namePrefix +"process.png"));
    }


    /**
     * 创建任务节点
     * 单人审批
     * @param id
     * @param name
     * @param assignee
     * @return
     */
    private UserTask createUserTask(String id, String name, String assignee) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee(assignee);
        return userTask;
    }

    /**
     * 连线
     * @param id
     * @param from
     * @param to
     * @return
     */
    private SequenceFlow createSequenceFlow(String id, String from, String to) {
        SequenceFlow flow = new SequenceFlow();
        flow.setId(id);
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        return flow;
    }


    /**
     * 开始节点
     * @return
     */
    private StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        return startEvent;
    }


    /**
     * 结束节点
     * @return
     */
    private EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");
        return endEvent;
    }

}
