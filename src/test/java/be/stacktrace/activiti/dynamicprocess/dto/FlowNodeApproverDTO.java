package be.stacktrace.activiti.dynamicprocess.dto;

import lombok.Data;

/**
 * @Project : sunlands-activiti
 * @Package Name : com.sunlands.feo.workflow.service.activitiflow
 * @Description : TODO
 * @Author : eleven
 * @Create Date : 2019年08月24日 13:39
 * @ModificationHistory Who   When     What
 * ------------    --------------    ---------------------------------
 */

@Data
public class FlowNodeApproverDTO {

    private String id;

    private String name;

    private String approved;

    public FlowNodeApproverDTO() {
    }

    public FlowNodeApproverDTO(String id, String name, String approved) {
        this.id = id;
        this.name = name;
        this.approved = approved;
    }
}
