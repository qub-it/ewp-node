package pt.ulisboa.ewp.node.api.host.forward.ewp.dto.omobilities.las.cnr;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(
    name = "",
    propOrder = {"maxOmobilityIds"})
@XmlRootElement(name = "omobility-la-cnr-api-specification-response")
public class ForwardEwpApiOutgoingMobilityLearningAgreementCnrApiSpecificationResponseDTO {

  @XmlElement(name = "max-omobility-ids", required = true)
  private int maxOmobilityIds;

  public ForwardEwpApiOutgoingMobilityLearningAgreementCnrApiSpecificationResponseDTO() {
  }

  public ForwardEwpApiOutgoingMobilityLearningAgreementCnrApiSpecificationResponseDTO(
      int maxOmobilityIds) {
    this.maxOmobilityIds = maxOmobilityIds;
  }

  public int getMaxOmobilityIds() {
    return maxOmobilityIds;
  }

  public void setMaxOmobilityIds(int maxOmobilityIds) {
    this.maxOmobilityIds = maxOmobilityIds;
  }
}
