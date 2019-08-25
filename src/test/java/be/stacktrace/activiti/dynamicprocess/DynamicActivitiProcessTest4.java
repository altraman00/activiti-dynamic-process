package be.stacktrace.activiti.dynamicprocess;

import org.activiti.bpmn.model.*;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicActivitiProcessTest4 {

    private static final Logger logger = LoggerFactory.getLogger(DynamicActivitiProcessTest4.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    public void testModifyFlowElement() {

        String processDefinitionId = "my-process-3:2:2604";

        String processDefinitionKey = "my-process-3";
        String businessKey = "4";

        Map<String, Object> variables = new HashMap<String, Object>() {{
            put("user", "zhangsan");
        }};
        ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey(processDefinitionKey, businessKey, variables);

//        BpmnModel bpmnModel = activitiRule.getRepositoryService().getBpmnModel(processInstance.getProcessDefinitionId());

        BpmnModel bpmnModel = activitiRule.getRepositoryService().getBpmnModel(processDefinitionId);

        FlowElement task1_2 = bpmnModel.getFlowElement("task1");


        List<SequenceFlow> incomingFlows_task1_2 = ((UserTask) task1_2).getIncomingFlows();

        logger.info("bpmnModel2:" + bpmnModel);


    }


    /**
     * 创建任务节点
     * 单人审批
     *
     * @param id
     * @param name
     * @param assignee
     * @return
     */
    protected UserTask createUserTask(String id, String name, String assignee, List<FormProperty> list) {
        UserTask userTask = new UserTask();
        userTask.setName(name);
        userTask.setId(id);
        userTask.setAssignee(assignee);
        userTask.setFormProperties(list);
        return userTask;
    }


    /**
     * 连线
     *
     * @param from
     * @param to
     * @return
     */
    protected SequenceFlow createSequenceFlow(String from, String to) {
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
    protected StartEvent createStartEvent() {
        StartEvent startEvent = new StartEvent();
        startEvent.setId("start");
        return startEvent;
    }

    /**
     * 结束节点
     *
     * @return
     */
    protected EndEvent createEndEvent() {
        EndEvent endEvent = new EndEvent();
        endEvent.setId("end");
        return endEvent;
    }


}
