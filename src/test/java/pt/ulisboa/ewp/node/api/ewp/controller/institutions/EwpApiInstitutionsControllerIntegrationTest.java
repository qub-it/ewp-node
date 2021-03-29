package pt.ulisboa.ewp.node.api.ewp.controller.institutions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import eu.erasmuswithoutpaper.api.institutions.v2.InstitutionsResponseV2;
import eu.erasmuswithoutpaper.api.institutions.v2.InstitutionsResponseV2.Hei;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import pt.ulisboa.ewp.host.plugin.skeleton.provider.InstitutionsHostProvider;
import pt.ulisboa.ewp.host.plugin.skeleton.provider.OrganizationalUnitsHostProvider;
import pt.ulisboa.ewp.node.EwpNodeApplication;
import pt.ulisboa.ewp.node.api.ewp.AbstractEwpControllerIntegrationTest;
import pt.ulisboa.ewp.node.api.ewp.utils.EwpApiConstants;
import pt.ulisboa.ewp.node.api.ewp.utils.EwpApiParamConstants;
import pt.ulisboa.ewp.node.client.ewp.registry.RegistryClient;
import pt.ulisboa.ewp.node.plugin.manager.host.HostPluginManager;
import pt.ulisboa.ewp.node.utils.XmlUtils;
import pt.ulisboa.ewp.node.utils.http.HttpParams;

@SpringBootTest(
    classes = {EwpNodeApplication.class, EwpApiInstitutionsControllerIntegrationTest.Config.class})
public class EwpApiInstitutionsControllerIntegrationTest
    extends AbstractEwpControllerIntegrationTest {

  @Autowired
  private HostPluginManager hostPluginManager;

  @Autowired
  private RegistryClient registryClient;

  @Configuration
  static class Config {

    @Bean
    @Primary
    public HostPluginManager getHostPluginManager() {
      return spy(new HostPluginManager(""));
    }

    @Bean
    @Primary
    public RegistryClient getRegistryClient() {
      return mock(RegistryClient.class);
    }
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpMethod.class,
      names = {"GET", "POST"})
  public void testInstitutionRetrieval_OneUnknownHeiId(HttpMethod method) throws Exception {
    String unknownHeiId = UUID.randomUUID().toString();

    Mockito.when(hostPluginManager.getProvider(unknownHeiId, OrganizationalUnitsHostProvider.class))
        .thenReturn(Optional.empty());

    HttpParams queryParams = new HttpParams();
    queryParams.param(EwpApiParamConstants.HEI_ID, unknownHeiId);

    String responseXml =
        executeRequest(
            registryClient, method, EwpApiConstants.API_BASE_URI + "institutions", queryParams)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    InstitutionsResponseV2 response =
        XmlUtils.unmarshall(responseXml, InstitutionsResponseV2.class);

    assertThat(response).isNotNull();
    assertThat(response.getHei()).hasSize(0);
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpMethod.class,
      names = {"GET", "POST"})
  public void testInstitutionRetrieval_OneValidHeiId(HttpMethod method) throws Exception {
    String validHeiId = UUID.randomUUID().toString();

    MockInstitutionsHostProvider mockProvider = new MockInstitutionsHostProvider();

    Hei hei = new Hei();
    hei.setHeiId(validHeiId);
    hei.setAbbreviation("TEST");
    mockProvider.register(hei);

    Mockito.when(hostPluginManager.getProvider(validHeiId, InstitutionsHostProvider.class))
        .thenReturn(Optional.of(mockProvider));

    HttpParams queryParams = new HttpParams();
    queryParams.param(EwpApiParamConstants.HEI_ID, validHeiId);

    String responseXml =
        executeRequest(
            registryClient, method, EwpApiConstants.API_BASE_URI + "institutions", queryParams)
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    InstitutionsResponseV2 response =
        XmlUtils.unmarshall(responseXml, InstitutionsResponseV2.class);

    assertThat(response).isNotNull();
    assertThat(response.getHei()).hasSize(1);
    assertThat(response.getHei().get(0).getHeiId()).isEqualTo(hei.getHeiId());
    assertThat(response.getHei().get(0).getAbbreviation()).isEqualTo(hei.getAbbreviation());
  }

  @ParameterizedTest
  @EnumSource(
      value = HttpMethod.class,
      names = {"GET", "POST"})
  public void testInstitutionRetrieval_MoreThanLimit(HttpMethod method) throws Exception {
    HttpParams queryParams = new HttpParams();
    queryParams.param(EwpApiParamConstants.HEI_ID, UUID.randomUUID().toString());
    queryParams.param(EwpApiParamConstants.HEI_ID, UUID.randomUUID().toString());
    assertBadRequest(
        registryClient,
        method,
        EwpApiConstants.API_BASE_URI + "institutions",
        queryParams,
        "Maximum number of valid HEI IDs per request is 1");
  }

  private static class MockInstitutionsHostProvider extends InstitutionsHostProvider {

    private final Map<String, Hei> heiIdToHeiMap = new HashMap<>();

    public void register(Hei hei) {
      this.heiIdToHeiMap.put(hei.getHeiId(), hei);
    }

    @Override
    public Optional<Hei> findByHeiId(String heiId) {
      return Optional.ofNullable(this.heiIdToHeiMap.get(heiId));
    }
  }
}
