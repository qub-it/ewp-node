package pt.ulisboa.ewp.node.api.ewp.controller.imobilities.tors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.erasmuswithoutpaper.api.imobilities.tors.v1.endpoints.ImobilityTorsGetResponseV1;
import eu.erasmuswithoutpaper.api.imobilities.tors.v1.endpoints.ImobilityTorsGetResponseV1.Tor;
import eu.erasmuswithoutpaper.api.imobilities.tors.v1.endpoints.ImobilityTorsGetResponseV1.Tor.GradeConversionTable;
import eu.erasmuswithoutpaper.api.imobilities.tors.v1.endpoints.ImobilityTorsGetResponseV1.Tor.GradeConversionTable.IscedTable;
import eu.erasmuswithoutpaper.api.imobilities.tors.v1.endpoints.ImobilityTorsIndexResponseV1;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import pt.ulisboa.ewp.host.plugin.skeleton.provider.imobilities.tors.IncomingMobilityToRsV1HostProvider;
import pt.ulisboa.ewp.host.plugin.skeleton.provider.imobilities.tors.MockIncomingMobilityToRsV1HostProvider;
import pt.ulisboa.ewp.node.api.ewp.AbstractEwpControllerIntegrationTest;
import pt.ulisboa.ewp.node.api.ewp.utils.EwpApiConstants;
import pt.ulisboa.ewp.node.api.ewp.utils.EwpApiParamConstants;
import pt.ulisboa.ewp.node.client.ewp.registry.RegistryClient;
import pt.ulisboa.ewp.node.domain.entity.mapping.EwpOutgoingMobilityMapping;
import pt.ulisboa.ewp.node.domain.repository.mapping.EwpOutgoingMobilityMappingRepository;
import pt.ulisboa.ewp.node.plugin.manager.host.HostPluginManager;
import pt.ulisboa.ewp.node.utils.XmlUtils;
import pt.ulisboa.ewp.node.utils.http.HttpParams;

class EwpApiIncomingMobilityToRsV1ControllerIntegrationTest extends
    AbstractEwpControllerIntegrationTest {

  @Autowired
  private HostPluginManager hostPluginManager;

  @Autowired
  private RegistryClient registryClient;

  @SpyBean
  private EwpOutgoingMobilityMappingRepository mappingRepository;

  @ParameterizedTest
  @EnumSource(
      value = HttpMethod.class,
      names = {"GET", "POST"})
  public void testOutgoingMobilityIdsWithTranscriptOfRecordsAttachedRetrieval_UnknownSendingHeiId_ErrorReturned(HttpMethod method)
      throws Exception {
    String unknownHeiId = UUID.randomUUID().toString();

    Mockito
        .when(hostPluginManager.getProvider(unknownHeiId,
            IncomingMobilityToRsV1HostProvider.class))
        .thenReturn(Optional.empty());

    HttpParams queryParams = new HttpParams();
    queryParams.param(EwpApiParamConstants.RECEIVING_HEI_ID, unknownHeiId);

    assertBadRequest(registryClient, method,
        EwpApiConstants.API_BASE_URI
            + EwpApiIncomingMobilityToRsV1Controller.BASE_PATH
            + "/index", queryParams, "Unknown HEI ID: " + unknownHeiId);
  }

  @ParameterizedTest
  @EnumSource(value = HttpMethod.class, names = {"GET", "POST"})
  public void testOutgoingMobilityIdsWithTranscriptOfRecordsAttachedRetrieval_ValidSendingHeiIdDividedIntoTwoHosts_AllOmobilityIdsReturned(
      HttpMethod method) throws Exception {
    String receivingHeiId = "test";
    List<String> omobilityIds = Arrays.asList("a1", "b2", "c3");

    MockIncomingMobilityToRsV1HostProvider mockProvider1 = new MockIncomingMobilityToRsV1HostProvider(
        3);
    MockIncomingMobilityToRsV1HostProvider mockProvider2 = new MockIncomingMobilityToRsV1HostProvider(
        3);

    mockProvider1.registerOutgoingMobilityIds(receivingHeiId, List.of(omobilityIds.get(0)));
    mockProvider2.registerOutgoingMobilityIds(receivingHeiId,
        List.of(omobilityIds.get(1), omobilityIds.get(2)));

    doReturn(true).when(hostPluginManager)
        .hasHostProvider(receivingHeiId, IncomingMobilityToRsV1HostProvider.class);
    doReturn(Arrays.asList(mockProvider1, mockProvider2)).when(hostPluginManager)
        .getAllProvidersOfType(receivingHeiId, IncomingMobilityToRsV1HostProvider.class);

    HttpParams queryParams = new HttpParams();
    queryParams.param(EwpApiParamConstants.RECEIVING_HEI_ID, receivingHeiId);

    String responseXml =
        executeRequest(registryClient, method,
            EwpApiConstants.API_BASE_URI
                + EwpApiIncomingMobilityToRsV1Controller.BASE_PATH
                + "/index",
            queryParams)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ImobilityTorsIndexResponseV1 response = XmlUtils.unmarshall(responseXml,
        ImobilityTorsIndexResponseV1.class);

    assertThat(response).isNotNull();
    assertThat(response.getOmobilityId()).isEqualTo(omobilityIds);
  }

  @ParameterizedTest
  @EnumSource(value = HttpMethod.class, names = {"GET", "POST"})
  public void testOutgoingMobilityToRsGetRetrievalByOmobilityIds_ValidHeiIdDividedIntoTwoHostsWithExistingMappings_AllToRsReturned(
      HttpMethod method) throws Exception {
    String receivingHeiId = "test";
    List<String> ounitIds = Arrays.asList("o1", "o2", "o3");
    List<String> omobilityIds = Arrays.asList("a1", "b2", "c3");

    List<Tor> tors = new ArrayList<>();
    for (int index = 0; index < omobilityIds.size(); index++) {
      Tor tor = new Tor();
      tor.setOmobilityId(omobilityIds.get(index));
      GradeConversionTable gradeConversionTable = new GradeConversionTable();
      IscedTable iscedTable = new IscedTable();
      iscedTable.setIscedCode("TEST");
      gradeConversionTable.getIscedTable().add(iscedTable);
      tors.add(tor);
    }

    MockIncomingMobilityToRsV1HostProvider mockProvider1 = new MockIncomingMobilityToRsV1HostProvider(
        3);
    MockIncomingMobilityToRsV1HostProvider mockProvider2 = new MockIncomingMobilityToRsV1HostProvider(
        3);

    mockProvider1.registerTranscriptOfRecords(receivingHeiId, omobilityIds.get(0),
        tors.get(0));

    mockProvider2.registerTranscriptOfRecords(receivingHeiId, omobilityIds.get(1),
        tors.get(1));
    mockProvider2.registerTranscriptOfRecords(receivingHeiId, omobilityIds.get(2),
        tors.get(2));

    for (int index = 0; index < omobilityIds.size(); index++) {
      doReturn(Optional.of(
          EwpOutgoingMobilityMapping.create(receivingHeiId, ounitIds.get(index),
              omobilityIds.get(index)))).when(mappingRepository)
          .findByHeiIdAndOmobilityId(receivingHeiId, omobilityIds.get(index));
    }

    doReturn(true).when(hostPluginManager)
        .hasHostProvider(receivingHeiId, IncomingMobilityToRsV1HostProvider.class);

    doReturn(Arrays.asList(mockProvider1, mockProvider2)).when(hostPluginManager)
        .getAllProvidersOfType(receivingHeiId,
            IncomingMobilityToRsV1HostProvider.class);

    doReturn(Arrays.asList(mockProvider1)).when(hostPluginManager)
        .getProvidersByHeiIdAndOunitId(receivingHeiId, ounitIds.get(0),
            IncomingMobilityToRsV1HostProvider.class);
    doReturn(Arrays.asList(mockProvider2)).when(hostPluginManager)
        .getProvidersByHeiIdAndOunitId(receivingHeiId, ounitIds.get(1),
            IncomingMobilityToRsV1HostProvider.class);
    doReturn(Arrays.asList(mockProvider2)).when(hostPluginManager)
        .getProvidersByHeiIdAndOunitId(receivingHeiId, ounitIds.get(2),
            IncomingMobilityToRsV1HostProvider.class);

    HttpParams queryParams = new HttpParams();
    queryParams.param(EwpApiParamConstants.RECEIVING_HEI_ID, receivingHeiId);
    queryParams.param(EwpApiParamConstants.OMOBILITY_ID, omobilityIds);

    String responseXml =
        executeRequest(registryClient, method,
            EwpApiConstants.API_BASE_URI
                + EwpApiIncomingMobilityToRsV1Controller.BASE_PATH
                + "/get",
            queryParams)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ImobilityTorsGetResponseV1 response = XmlUtils.unmarshall(responseXml,
        ImobilityTorsGetResponseV1.class);

    assertThat(response).isNotNull();
    assertThat(response.getTor()).hasSize(omobilityIds.size());
    for (Tor tor : tors) {
      assertThat(response.getTor().stream()
          .map(Tor::getOmobilityId)
          .collect(Collectors.toList())).contains(tor.getOmobilityId());
    }
  }

  @ParameterizedTest
  @EnumSource(value = HttpMethod.class, names = {"GET", "POST"})
  public void testOutgoingMobilityToRsGetRetrievalByOmobilityIds_ValidHeiIdDividedIntoTwoHostsWithAllButOneExistingMappings_AllKnownToRsReturned(
      HttpMethod method) throws Exception {
    String receivingHeiId = "test";
    List<String> ounitIds = Arrays.asList("o1", "o2", "o3");
    List<String> omobilityIds = Arrays.asList("a1", "b2", "c3");
    List<String> knownOmobilityIds = omobilityIds.subList(0, 2);

    List<Tor> tors = new ArrayList<>();
    for (int index = 0; index < knownOmobilityIds.size(); index++) {
      Tor tor = new Tor();
      tor.setOmobilityId(knownOmobilityIds.get(index));
      GradeConversionTable gradeConversionTable = new GradeConversionTable();
      IscedTable iscedTable = new IscedTable();
      iscedTable.setIscedCode("TEST");
      gradeConversionTable.getIscedTable().add(iscedTable);
      tors.add(tor);
    }

    MockIncomingMobilityToRsV1HostProvider mockProvider1 = new MockIncomingMobilityToRsV1HostProvider(
        3);
    MockIncomingMobilityToRsV1HostProvider mockProvider2 = new MockIncomingMobilityToRsV1HostProvider(
        3);

    mockProvider1.registerTranscriptOfRecords(receivingHeiId, omobilityIds.get(0),
        tors.get(0));

    mockProvider2.registerTranscriptOfRecords(receivingHeiId, omobilityIds.get(1),
        tors.get(1));

    for (int index = 0; index < knownOmobilityIds.size(); index++) {
      doReturn(Optional.of(
          EwpOutgoingMobilityMapping.create(receivingHeiId, ounitIds.get(index),
              knownOmobilityIds.get(index)))).when(mappingRepository)
          .findByHeiIdAndOmobilityId(receivingHeiId, knownOmobilityIds.get(index));
    }

    doReturn(true).when(hostPluginManager)
        .hasHostProvider(receivingHeiId, IncomingMobilityToRsV1HostProvider.class);

    doReturn(Arrays.asList(mockProvider1, mockProvider2)).when(hostPluginManager)
        .getAllProvidersOfType(receivingHeiId,
            IncomingMobilityToRsV1HostProvider.class);

    doReturn(Arrays.asList(mockProvider1)).when(hostPluginManager)
        .getProvidersByHeiIdAndOunitId(receivingHeiId, ounitIds.get(0),
            IncomingMobilityToRsV1HostProvider.class);
    doReturn(Arrays.asList(mockProvider2)).when(hostPluginManager)
        .getProvidersByHeiIdAndOunitId(receivingHeiId, ounitIds.get(1),
            IncomingMobilityToRsV1HostProvider.class);
    doReturn(Arrays.asList(mockProvider2)).when(hostPluginManager)
        .getProvidersByHeiIdAndOunitId(receivingHeiId, ounitIds.get(2),
            IncomingMobilityToRsV1HostProvider.class);

    HttpParams queryParams = new HttpParams();
    queryParams.param(EwpApiParamConstants.RECEIVING_HEI_ID, receivingHeiId);
    queryParams.param(EwpApiParamConstants.OMOBILITY_ID, omobilityIds);

    String responseXml =
        executeRequest(registryClient, method,
            EwpApiConstants.API_BASE_URI
                + EwpApiIncomingMobilityToRsV1Controller.BASE_PATH
                + "/get",
            queryParams)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    ImobilityTorsGetResponseV1 response = XmlUtils.unmarshall(responseXml,
        ImobilityTorsGetResponseV1.class);

    assertThat(response).isNotNull();
    assertThat(response.getTor()).hasSize(knownOmobilityIds.size());
    for (Tor tor : tors) {
      assertThat(response.getTor().stream()
          .map(Tor::getOmobilityId)
          .collect(Collectors.toList())).contains(tor.getOmobilityId());
    }
  }

}