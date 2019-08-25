package be.stacktrace.activiti.dynamicprocess;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.google.common.collect.Lists;
import junit.framework.Assert;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Rule;
import org.junit.Test;

public class DynamicActivitiProcessTest {

  @Rule
  public ActivitiRule activitiRule = new ActivitiRule();

  @Test
  public void testDynamicDeploy() throws Exception {

    String processDefinitionKey = "my-process-9";
    String businessKey = "1";

    // 1. Build up the model from scratch
    BpmnModel model = new BpmnModel();
    Process process = new Process();
    model.addProcess(process);
    process.setId(processDefinitionKey);

    List<FormProperty> list = Lists.newArrayList();
    FormProperty form1 = new FormProperty();
    form1.setId("action");
    form1.setName("动作");
    list.add(form1);

    FormProperty form2 = new FormProperty();
    form2.setId("msg");
    form2.setName("审批信息");
    list.add(form2);


    process.addFlowElement(createStartEvent());
    process.addFlowElement(createUserTask("task1", "First task", "fred",list));
    process.addFlowElement(createUserTask("task2", "Second task", "${approveUser}",list));
    process.addFlowElement(createEndEvent());

    process.addFlowElement(createSequenceFlow("start", "task1",null));
    process.addFlowElement(createSequenceFlow("task1", "gateway1",null));
    process.addFlowElement(createExclusiveGateway("gateway1","task1网关"));
    process.addFlowElement(createSequenceFlow("gateway1", "task2","${action == \"y\" || action == \"Y\"}"));
    process.addFlowElement(createSequenceFlow("gateway1", "end","${action == \"y\" || action == \"Y\"}"));
    process.addFlowElement(createSequenceFlow("task2", "end",null));

    // 2. Generate graphical information
    new BpmnAutoLayout(model).execute();

    // 3. Deploy the process to the engine
    Deployment deployment = activitiRule.getRepositoryService().createDeployment()
        .addBpmnModel("dynamic-model.bpmn", model).name("Dynamic process deployment").deploy();

    // 4. Start a process instance
    ProcessInstance processInstance = activitiRule.getRuntimeService()
        .startProcessInstanceByKey(processDefinitionKey,businessKey);

    // 5. Check if task is available
    List<Task> tasks = activitiRule.getTaskService().createTaskQuery()
      .processInstanceId(processInstance.getId()).list();

    Assert.assertEquals(1, tasks.size());
    Assert.assertEquals("First task", tasks.get(0).getName());
    Assert.assertEquals("fred", tasks.get(0).getAssignee());

    // 6. Save process diagram to a file
//    InputStream processDiagram = activitiRule.getRepositoryService().getProcessDiagram(processInstance.getProcessDefinitionId());
//    FileUtils.copyInputStreamToFile(processDiagram, new File("target/"+ processDefinitionKey +".png"));

    // 7. Save resulting BPMN xml to a file
    InputStream processBpmn = activitiRule.getRepositoryService().getResourceAsStream(deployment.getId(), "dynamic-model.bpmn");
    FileUtils.copyInputStreamToFile(processBpmn, new File("target/"+ processDefinitionKey +".bpmn.xml"));

  }


  /**
   * 创建任务节点
   * 单人审批
   * @param id
   * @param name
   * @param assignee
   * @return
   */
  protected UserTask createUserTask(String id, String name, String assignee,List<FormProperty> list) {
    UserTask userTask = new UserTask();
    userTask.setName(name);
    userTask.setId(id);
    userTask.setAssignee(assignee);
    userTask.setFormProperties(list);
    return userTask;
  }


  /**
   * 连线 SequenceFlow中添加ConditionExpression
   * @param from
   * @param to
   * @return
   */
  protected SequenceFlow createSequenceFlow(String from, String to,String conditionExpressio) {
    SequenceFlow flow = new SequenceFlow();
    flow.setSourceRef(from);
    flow.setTargetRef(to);
    if(StringUtils.isNotEmpty(conditionExpressio)){
      flow.setConditionExpression(conditionExpressio);
    }
    return flow;
  }

  /**
   * 创建排他网关  由前面SequenceFlow的targetId指向网关id，后面的SequenceFlow的sourceId也指向网关id
   * @return
   */
  protected ExclusiveGateway createExclusiveGateway(String gatewayId, String gatewayName){
    ExclusiveGateway gateway = new ExclusiveGateway();
    gateway.setId(gatewayId);
    gateway.setName(gatewayName);
    return gateway;
  }


  /**
   * 开始节点
   * @return
   */
  protected StartEvent createStartEvent() {
    StartEvent startEvent = new StartEvent();
    startEvent.setId("start");
    return startEvent;
  }

  /**
   * 结束节点
   * @return
   */
  protected EndEvent createEndEvent() {
    EndEvent endEvent = new EndEvent();
    endEvent.setId("end");
    return endEvent;
  }



}
