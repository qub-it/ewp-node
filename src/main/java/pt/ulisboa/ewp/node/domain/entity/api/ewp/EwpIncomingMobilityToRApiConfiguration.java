package pt.ulisboa.ewp.node.domain.entity.api.ewp;

import eu.erasmuswithoutpaper.api.imobilities.tors.v1.ImobilityTorsV1;
import java.math.BigInteger;
import java.util.Collection;
import pt.ulisboa.ewp.node.api.ewp.utils.EwpApiUtils;
import pt.ulisboa.ewp.node.domain.entity.api.ewp.auth.client.EwpClientAuthenticationConfiguration;
import pt.ulisboa.ewp.node.domain.entity.api.ewp.auth.server.EwpServerAuthenticationConfiguration;

public class EwpIncomingMobilityToRApiConfiguration extends EwpApiConfiguration {

  public static final String API_NAME = "Incoming Mobility ToRs API";

  private String getUrl;
  private String indexUrl;
  private BigInteger maxOmobilityIds;
  private boolean sendsNotifications;

  public EwpIncomingMobilityToRApiConfiguration(
      String getUrl,
      String indexUrl,
      Collection<EwpClientAuthenticationConfiguration> supportedClientAuthenticationMethods,
      Collection<EwpServerAuthenticationConfiguration> supportedServerAuthenticationMethods,
      BigInteger maxOmobilityIds,
      boolean sendsNotifications) {
    super(supportedClientAuthenticationMethods, supportedServerAuthenticationMethods);
    this.getUrl = getUrl;
    this.indexUrl = indexUrl;
    this.maxOmobilityIds = maxOmobilityIds;
    this.sendsNotifications = sendsNotifications;
  }

  public String getGetUrl() {
    return getUrl;
  }

  public void setGetUrl(String getUrl) {
    this.getUrl = getUrl;
  }

  public String getIndexUrl() {
    return indexUrl;
  }

  public void setIndexUrl(String indexUrl) {
    this.indexUrl = indexUrl;
  }

  public BigInteger getMaxOmobilityIds() {
    return maxOmobilityIds;
  }

  public void setMaxOmobilityIds(BigInteger maxOmobilityIds) {
    this.maxOmobilityIds = maxOmobilityIds;
  }

  public boolean isSendsNotifications() {
    return sendsNotifications;
  }

  public void setSendsNotifications(boolean sendsNotifications) {
    this.sendsNotifications = sendsNotifications;
  }

  public static EwpIncomingMobilityToRApiConfiguration create(ImobilityTorsV1 apiElement) {
    return new EwpIncomingMobilityToRApiConfiguration(
        apiElement.getGetUrl(),
        apiElement.getIndexUrl(),
        EwpApiUtils.getSupportedClientAuthenticationMethods(apiElement.getHttpSecurity()),
        EwpApiUtils.getSupportedServerAuthenticationMethods(apiElement.getHttpSecurity()),
        apiElement.getMaxOmobilityIds(),
        apiElement.getSendsNotifications() != null);
  }

  @Override
  public String toString() {
    return "EwpIncomingMobilityToRApiConfiguration{" +
        "getUrl='" + getUrl + '\'' +
        ", indexUrl='" + indexUrl + '\'' +
        ", maxOmobilityIds=" + maxOmobilityIds +
        ", sendsNotifications=" + sendsNotifications +
        '}';
  }
}
