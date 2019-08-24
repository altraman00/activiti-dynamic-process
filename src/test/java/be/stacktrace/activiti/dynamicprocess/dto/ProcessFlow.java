package be.stacktrace.activiti.dynamicprocess.dto;

import lombok.Data;

import java.util.List;

/**
 * @Project : sunlands-activiti
 * @Package Name : com.sunlands.feo.workflow.service.activitiflow
 * @Description : TODO
 * @Author : eleven
 * @Create Date : 2019年08月24日 13:35
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */
@Data
public class ProcessFlow {

    private String id;

    private String name;

    private List<Approvers> processNodes;

    public ProcessFlow() {
    }

    public ProcessFlow(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ProcessFlow(String id, String name, List<Approvers> processNodes) {
        this.id = id;
        this.name = name;
        this.processNodes = processNodes;
    }
}
