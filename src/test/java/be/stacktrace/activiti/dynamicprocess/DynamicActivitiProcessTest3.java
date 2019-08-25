package be.stacktrace.activiti.dynamicprocess;

import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.*;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicActivitiProcessTest3 {

    private static final Logger logger = LoggerFactory.getLogger(DynamicActivitiProcessTest3.class);

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    public void testModifyFlowElement() {

//        String processDefinitionKey = "my-process-3";
//        String businessKey = "4";
//
//        Map<String,Object> variables = new HashMap<String,Object>(){{
//            put("user","zhangsan");
//        }};
//        ProcessInstance processInstance = activitiRule.getRuntimeService().startProcessInstanceByKey(processDefinitionKey, businessKey, variables);

//        BpmnModel bpmnModel = activitiRule.getRepositoryService().getBpmnModel(processInstance.getProcessDefinitionId());

        BpmnModel bpmnModel = activitiRule.getRepositoryService().getBpmnModel("my-process-3:2:2604");

        FlowElement start = bpmnModel.getFlowElement("start");
        FlowElement task1 = bpmnModel.getFlowElement("task1");
        FlowElement task2 = bpmnModel.getFlowElement("task2");
        FlowElement end = bpmnModel.getFlowElement("end");

        List<SequenceFlow> incomingFlows_start = ((UserTask) start).getIncomingFlows();
        List<SequenceFlow> incomingFlows_task1 = ((UserTask) task1).getIncomingFlows();
        List<SequenceFlow> incomingFlows_task2 = ((UserTask) task2).getIncomingFlows();
        List<SequenceFlow> incomingFlows_end = ((UserTask) end).getIncomingFlows();

        String sourceRef_start = incomingFlows_start.get(0).getSourceRef();
        String targetRef_start = incomingFlows_start.get(0).getTargetRef();

        String sourceRef_task1 = incomingFlows_task1.get(0).getSourceRef();
        String targetRef_task1 = incomingFlows_task1.get(0).getTargetRef();

        String sourceRef_task2 = incomingFlows_task2.get(0).getSourceRef();
        String targetRef_task2 = incomingFlows_task2.get(0).getTargetRef();

        String sourceRef_end = incomingFlows_end.get(0).getSourceRef();
        String targetRef_end = incomingFlows_end.get(0).getTargetRef();


        incomingFlows_task1.add(createSequenceFlow("task1","end"));

        BpmnModel bpmnModel2 = activitiRule.getRepositoryService().getBpmnModel("my-process-3:2:2604");
        FlowElement task1_2 = bpmnModel.getFlowElement("task1");
        List<SequenceFlow> incomingFlows_task1_2 = ((UserTask) task1).getIncomingFlows();

        logger.info("bpmnModel2:"+bpmnModel2);


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
