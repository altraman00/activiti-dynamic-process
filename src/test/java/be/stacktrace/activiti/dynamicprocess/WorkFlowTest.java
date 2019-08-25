package be.stacktrace.activiti.dynamicprocess;

import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @Project : activiti-dynamic
 * @Package Name : be.stacktrace.activiti.dynamicprocess
 * @Description : TODO
 * @Author : eleven
 * @Create Date : 2019年08月24日 15:34
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
public class WorkFlowTest {

    private static final Logger logger = LoggerFactory.getLogger(WorkFlowTest.class);

    String processDefinitionKey = "my-process-3";
    String businessKey = "3";

    @Rule
    public ActivitiRule activitiRule = new ActivitiRule();

    @Test
    public void start() {
        Map<String,Object> variables = new HashMap<String,Object>(){{
            put("user","zhangsan");
        }};
        activitiRule.getRuntimeService().startProcessInstanceByKey(processDefinitionKey,businessKey,variables);
    }

    @Test
    public void apply() {
        Task task = activitiRule.getTaskService().createTaskQuery().processInstanceBusinessKey(businessKey).singleResult();

        logger.info("taskid:{},taskName:{}",task.getId(),task.getName());
    }


}
