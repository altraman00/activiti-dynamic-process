package be.stacktrace.activiti.dynamicprocess.dto;

import lombok.Data;

import java.util.List;

/**
 * @Project : sunlands-activiti
 * @Package Name : com.sunlands.feo.workflow.service.activitiflow
 * @Description : TODO
 * @Author : eleven
 * @Create Date : 2019年08月24日 13:41
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Data
public class FlowNodeDTO {

    private String id;

    private String name;

    private List<FlowNodeApproverDTO> approvers;

    public FlowNodeDTO() {
    }

    public FlowNodeDTO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public FlowNodeDTO(String id, String name, List<FlowNodeApproverDTO> approvers) {
        this.id = id;
        this.name = name;
        this.approvers = approvers;
    }
}
