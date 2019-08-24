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
public class ProcessFlowDTO {

    private String id;

    private String name;

    private List<FlowNodeDTO> processNodes;

    public ProcessFlowDTO() {
    }

    public ProcessFlowDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public ProcessFlowDTO(String id, String name, List<FlowNodeDTO> processNodes) {
        this.id = id;
        this.name = name;
        this.processNodes = processNodes;
    }
}
