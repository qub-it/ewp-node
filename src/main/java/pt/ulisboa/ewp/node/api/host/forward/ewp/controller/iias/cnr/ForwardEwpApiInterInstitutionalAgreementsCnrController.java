package pt.ulisboa.ewp.node.api.host.forward.ewp.controller.iias.cnr;

import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pt.ulisboa.ewp.node.api.host.forward.ewp.controller.AbstractForwardEwpApiController;
import pt.ulisboa.ewp.node.api.host.forward.ewp.controller.ForwardEwpApi;
import pt.ulisboa.ewp.node.api.host.forward.ewp.dto.ForwardEwpApiResponse;
import pt.ulisboa.ewp.node.api.host.forward.ewp.dto.iias.cnr.ForwardEwpApiInterInstitutionalAgreementCnrRequestDto;
import pt.ulisboa.ewp.node.api.host.forward.ewp.security.ForwardEwpApiSecurityCommonConstants;
import pt.ulisboa.ewp.node.api.host.forward.ewp.utils.ForwardEwpApiConstants;
import pt.ulisboa.ewp.node.api.host.forward.ewp.utils.ForwardEwpApiResponseUtils;
import pt.ulisboa.ewp.node.client.ewp.registry.RegistryClient;
import pt.ulisboa.ewp.node.domain.entity.notification.EwpInterInstitutionalAgreementChangeNotification;
import pt.ulisboa.ewp.node.domain.repository.notification.EwpChangeNotificationRepository;
import pt.ulisboa.ewp.node.service.ewp.mapping.EwpInterInstitutionalAgreementMappingService;
import pt.ulisboa.ewp.node.utils.EwpApi;

@RestController
@ForwardEwpApi(EwpApi.INTERINSTITUTIONAL_AGREEMENT_CNR)
@RequestMapping(ForwardEwpApiConstants.API_BASE_URI + "iias/cnr")
@Secured({ForwardEwpApiSecurityCommonConstants.ROLE_HOST_WITH_PREFIX})
public class ForwardEwpApiInterInstitutionalAgreementsCnrController extends
    AbstractForwardEwpApiController {

  private final EwpChangeNotificationRepository changeNotificationRepository;
  private final EwpInterInstitutionalAgreementMappingService interInstitutionalAgreementMappingService;

  public ForwardEwpApiInterInstitutionalAgreementsCnrController(
      RegistryClient registryClient,
      EwpChangeNotificationRepository changeNotificationRepository,
      EwpInterInstitutionalAgreementMappingService interInstitutionalAgreementMappingService) {
    super(registryClient);
    this.changeNotificationRepository = changeNotificationRepository;
    this.interInstitutionalAgreementMappingService = interInstitutionalAgreementMappingService;
  }

  @PostMapping(
      consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
      produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<ForwardEwpApiResponse>
  sendChangeNotification(@Valid ForwardEwpApiInterInstitutionalAgreementCnrRequestDto requestDto) {
    int index = 0;
    for (String iiaId : requestDto.getIiaIds()) {
      EwpInterInstitutionalAgreementChangeNotification changeNotification = new EwpInterInstitutionalAgreementChangeNotification(
          requestDto.getNotifierHeiId(),
          requestDto.getPartnerHeiId(), iiaId);
      changeNotificationRepository.persist(changeNotification);

      interInstitutionalAgreementMappingService.registerMapping(requestDto.getNotifierHeiId(),
          requestDto.getNotifierOunitId(), iiaId,
          requestDto.getIiaCodes().get(index));

      index++;
    }
    return ForwardEwpApiResponseUtils.toAcceptedResponseEntity();
  }
}
