package be.stacktrace.activiti.dynamicprocess;

import be.stacktrace.activiti.dynamicprocess.dto.Approvers;
import be.stacktrace.activiti.dynamicprocess.dto.AuditProcessNodeApprover;
import be.stacktrace.activiti.dynamicprocess.dto.ProcessFlow;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Project : sunlands-activiti
 * @Package Name : com.sunlands.feo.workflow.service.activitiflow
 * @Description : TODO
 * @Author : eleven
 * @Create Date : 2019年08月24日 13:18
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

public class DynamicActivitiProcessTest2 {

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    private static final Logger logger = LoggerFactory.getLogger(DynamicActivitiProcessTest2.class);

    static String processDefinitionKey = "my-process-1";

    @Test
    public void testDynamicDeploy123() {


        String businessKey = "5";

        //1、审核人
        final AuditProcessNodeApprover apna1 = new AuditProcessNodeApprover("process_1","AuditProcessNodeApprover_name_1","AuditProcessNodeApprover_approved_1");
        final AuditProcessNodeApprover apna2 = new AuditProcessNodeApprover("process_2","AuditProcessNodeApprover_name_2","AuditProcessNodeApprover_approved_2");
        List<AuditProcessNodeApprover> approverList = new ArrayList<AuditProcessNodeApprover>(){{
            add(apna1);
            add(apna2);
        }};

        //2、审核节点
        final Approvers approvers1 = new Approvers("approvers_id_1","approvers_name_1",approverList);
        final Approvers approvers2 = new Approvers("approvers_id_2","approvers_name_2",approverList);
        List<Approvers> processNodes = new ArrayList<Approvers>(){{
            add(approvers1);
            add(approvers2);
        }};

        //3、审批流
        ProcessFlow processFlow = new ProcessFlow(processDefinitionKey,"process_flow_test_1",processNodes);

        deployProcess(processFlow);
    }


    /**
     * 部署流程
     * @param processFlow
     * @return
     */
    public Integer deployProcess(ProcessFlow processFlow) {

        try {
            BpmnModel model = new BpmnModel();
            Process process = new Process();
            model.addProcess(process);

            /**
             *process的id不能以数字开头
             */
            process.setId(processFlow.getId());
            List<String> users;

            /**
             *获取流程的节点数量
             */
            int size = processFlow.getProcessNodes().size();
            process.addFlowElement(createStartEvent());

            String ProcessIDPrefix = "sunlands";

            /**
             *生成流程
             */
            for (int i = 0, j = size; i < j; i++) {
                users = new ArrayList<String>();
                for (AuditProcessNodeApprover approver : processFlow.getProcessNodes().get(i).getApprovers()) {
                    users.add(approver.getApproved());
                }
                process.addFlowElement(createUsersTask(ProcessIDPrefix + processFlow.getProcessNodes().get(i).getId(), processFlow.getProcessNodes().get(i).getName(), users));
                if (i == 0) {
                    process.addFlowElement(createSequenceFlow("startEvent", ProcessIDPrefix + processFlow.getProcessNodes().get(i).getId()));
                }else {
                    process.addFlowElement(createSequenceFlow(ProcessIDPrefix + processFlow.getProcessNodes().get(i - 1).getId(), ProcessIDPrefix + processFlow.getProcessNodes().get(i).getId()));
                }
                if (i == size - 1) {
                    process.addFlowElement(createSequenceFlow(ProcessIDPrefix + processFlow.getProcessNodes().get(i).getId(), "endEvent"));
                }
            }
            process.addFlowElement(createEndEvent());

            /**
             * 生成图形信息
             */
            new BpmnAutoLayout(model).execute();

            //将流程部署到引擎
            Deployment deployment = activitiRule.getRepositoryService().createDeployment()
                    .addBpmnModel(process.getId() + ".bpmn", model).name(processFlow.getName())
                    .deploy();

            // 4. Start a process instance
            ProcessInstance processInstance = activitiRule.getRuntimeService()
                    .startProcessInstanceByKey(processFlow.getId(),processFlow.getId());

            // 5. Check if task is available
            List<Task> tasks = activitiRule.getTaskService().createTaskQuery()
                    .processInstanceId(processInstance.getId()).list();

            Assert.assertEquals(1, tasks.size());
//            Assert.assertEquals("First task", tasks.get(0).getName());
//            Assert.assertEquals("fred", tasks.get(0).getAssignee());

            // 6. Save process diagram to a file
            InputStream processDiagram = activitiRule.getRepositoryService().getProcessDiagram(processInstance.getProcessDefinitionId());
            FileUtils.copyInputStreamToFile(processDiagram, new File("target/" + processDefinitionKey +".png"));

            // 7. Save resulting BPMN xml to a file
            InputStream processBpmn = activitiRule.getRepositoryService().getResourceAsStream(deployment.getId(), process.getId() + ".bpmn");
            FileUtils.copyInputStreamToFile(processBpmn, new File("target/" + processDefinitionKey + ".bpmn.xml"));

        } catch (Exception e) {
            logger.error("添加流程异常",e);
        }
        return 1;

    }


    /**
     * 创建任务节点
     * 多人审批
     */
    public static UserTask createUsersTask(String id, String name, List<String> assignee) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setCandidateUsers(assignee);
        return userTask;
    }

    /**
     * 创建任务节点
     * 单人审批
     */
    public static UserTask createUserTask(String id, String name, String assignee) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee(assignee);
        return userTask;
    }

    /**
     * 连线
     *
     * @param from
     * @param to
     * @return
     */
    public static SequenceFlow createSequenceFlow(String from, String to) {
        SequenceFlow flow = new SequenceFlow();
        flow.setSourceRef(from);
        flow.setTargetRef(to);
        return flow;
    }

    /**
     * 开始节点
     *
     * @return
     */
    public static StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent");
        startEvent.setName("start");
        return startEvent;
    }

    /**
     * 结束节点
     *
     * @return
     */
    public static EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent");
        endEvent.setName("end");
        return endEvent;
    }


    /**
     * 申请人已申请任务(完成状态)[学习使用]
     *
     * @return
     */
    public Object queryApply(String processDefinitionKey, String user) {
        List<HistoricProcessInstance> hisProInstance = activitiRule.getHistoryService().createHistoricProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey).startedBy(user).finished()
                .orderByProcessInstanceEndTime().desc().list();
        for (HistoricProcessInstance hisInstance : hisProInstance) {
            System.out.println("发起人 :" + hisInstance.getStartUserId());
            System.out.println("发起时间 :" + hisInstance.getStartTime());
        }
        return "已申请任务";
    }

}
