@Override
public ActualizarConsentimientoResponse agregarExtra(AgregarExtraRequest request, String idConsentimiento) {
    log.info("[agregarExtra][BCI_INI] Inicio llamada api agregar extra consentimiento: {}]", request);


    ApiAgregarExtraRequestDto apiRequest = apiConsentMapper.requestExtraDomainToApiDto(request);
    HttpHeaders headers = crearHeaders(request.getFirma(), jwtConfiguration.getProductoIdPublico());


    ApiActualizarResponseDto responseDto = webClientExternal.patch()
            .uri(endpointConfiguration.getAgregarExtrasConsentimiento()
                    .replace("{idConsentimiento}", idConsentimiento))
            .headers(httpHeaders -> httpHeaders.addAll(headers))
            .bodyValue(apiRequest)
            .retrieve()
            .onStatus(httpStatus -> httpStatus.value() == 409, manejarError409())
            .onStatus(httpStatus -> httpStatus.value() == 403, manejarError403())
            .onStatus(HttpStatus::is4xxClientError, manejarError4xx())
            .onStatus(httpStatus -> httpStatus.value() == 207, manejarError207())
            .onStatus(HttpStatus::is5xxServerError, manejarError5xx())
            .bodyToMono(ApiActualizarResponseDto.class)
            .block();
…}


Si dicha definicion:
        package cl.bci.consentimientos.repository.apiconsent.client.dto;




import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Clase que contiene la respuesta del api al actualizar consentimiento.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiActualizarResponseDto {


    @JsonProperty("Data")
    private DataDto data;


    @JsonProperty("Links")
    private ApiConsentimientoResponseDto.LinksDto links;


    @JsonProperty("Meta")
    private HashMap<String, Object> meta;


    /**
     * Clase que contiene los datos del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataDto {


        @JsonProperty("Uuid")
        private String uuid;


        @JsonProperty("CustomerId")
        private String customerId;


        @JsonProperty("CustomerName")
        private String customerName;


        @JsonProperty("CustomerCompanyId")
        private String customerCompanyId;


        @JsonProperty("PartnerId")
        private String partnerId;


        @JsonProperty("PartnerName")
        private String partnerName;


        @JsonProperty("ClientType")
        private String clientType;


        @JsonProperty("Audit")
        private AuditDto audit;


        @JsonProperty("Scope")
        private String scope;


        @JsonProperty("FintechName")
        private String fintechName;


        @JsonProperty("Status")
        private String status;


        @JsonProperty("Channel")
        private String channel;


        @JsonProperty("SearchPeriod")
        private ApiConsentimientoResponseDto.SearchPeriodDto searchPeriod;


        @JsonProperty("Recurrence")
        private ApiConsentimientoResponseDto.RecurrenceDto recurrence;


        @JsonProperty("Validity")
        private ApiConsentimientoResponseDto.ValidityDto validity;


        @JsonProperty("Ran207Consent")
        private boolean ran207Consent;


        @JsonProperty("CredentialAsignement")
        private boolean credentialAsignement;


        @JsonProperty("PartnerConfigId")
        private String partnerConfigId;


        @JsonProperty("Extras")
        private ExtraDto extras;
    }




    /**
     * Clase que contiene la auditoria de la modificacion.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuditDto {


        @JsonProperty("CreationDate")
        private String creationDate;


        @JsonProperty("ModificationDate")
        private String modificationDate;


        @JsonProperty("ExpiryDate")
        private String expiryDate;


        @JsonProperty("BrowserInfo")
        private String browserInfo;


        @JsonProperty("AuthorizationTime")
        private String authorizationTime;


        @JsonProperty("HistoricalModification")
        private List<ApiConsentimientoResponseDto.HistoricalModificationDto> historicalModification;


        @JsonProperty("ClientIp")
        private String clientIp;


        @JsonProperty("Authorization")
        private ApiConsentimientoResponseDto.AuthorizationDetailsDto authorization;
    }


    /**
     * Clase que contiene los datos extras del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExtraDto {


        @JsonProperty("Group")
        private List<GroupDto> group;
    }


    /**
     * clase que contiene el grupo de datos extras del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GroupDto {


        @JsonProperty("Reference")
        private String reference;


        @JsonProperty("AdditionalInfo")
        private String additionalInfo;


        @JsonProperty("Type")
        private String type;


        @JsonProperty("CreationDate")
        private String creationDate;


        @JsonProperty("Metadata")
        private List<MetadataDto> metadata;


        @JsonProperty("HistoricalModification")
        private List<HistoricalModificationExtraDto> historicalModification;
    }


    /**
     * Clase que contiene los metadatos del grupo de datos extras del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MetadataDto {


        @JsonProperty("Property")
        private String property;


        @JsonProperty("Value")
        private String value;
    }


    /**
     * Clase que contiene el historial de cambios del grupo de datos extras del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HistoricalModificationExtraDto {




        @JsonProperty("Date")
        private String date;


        @JsonProperty("Reason")
        private String reason;


        @JsonProperty("ChangeAuthor")
        private String changeAuthor;


        @JsonProperty("Metadata")
        private List<MetadataDto> metadata;
    }
}
package cl.bci.consentimientos.repository.apiconsent.client.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * Clase que contiene la respuesta del api consentimiento.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiConsentimientoResponseDto {


    @JsonProperty("Data")
    private DataDto data;


    @JsonProperty("Links")
    private LinksDto links;


    @JsonProperty("Meta")
    private HashMap<String, Object> meta;


    /**
     * Clase que contiene la data del api consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataDto {


        @JsonProperty("Scope")
        private String scope;


        @JsonProperty("ClientType")
        private String clientType;


        @JsonProperty("OTPRequired")
        private String otpRequired;


        @JsonProperty("DataConsent")
        private List<DataConsentDto> dataConsent;


        @JsonProperty("Authorizations")
        private List<AuthorizationDto> authorizations;


        @JsonProperty("SearchPeriod")
        private SearchPeriodDto searchPeriod;


        @JsonProperty("Recurrence")
        private RecurrenceDto recurrence;


        @JsonProperty("Validity")
        private ValidityDto validity;


        @JsonProperty("FintechName")
        private String fintechName;


        @JsonProperty("TraceId")
        private String traceId;


        @JsonProperty("Consent")
        private ConsentDto consent;
    }


    /**
     * Clase que contiene los datos del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DataConsentDto {


        @JsonProperty("Title")
        private String title;


        @JsonProperty("ScopeInfo")
        private List<ScopeInfoDto> scopeInfo;
    }


    /**
     * Clase que contiene la respuesta del ambito del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScopeInfoDto {


        @JsonProperty("Order")
        private int order;


        @JsonProperty("DetailedData")
        private String detailedData;
    }


    /**
     * Clase que contiene la respuesta de la autorizacion del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorizationDto {


        @JsonProperty("Order")
        private int order;


        @JsonProperty("TextData")
        private String textData;


        @JsonProperty("LinkData")
        private String linkData;
    }


    /**
     * Clase que contiene la respuesta del periodo de busquedad del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SearchPeriodDto {


        @JsonProperty("Enable")
        private boolean enable;


        @JsonProperty("Since")
        private String since;


        @JsonProperty("Until")
        private String until;


        @JsonProperty("NumberOfRecords")
        private int numberOfRecords;


        @JsonProperty("Duration")
        private String duration;
    }


    /**
     * Clase que contiene la respuesta del la recurrencia del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RecurrenceDto {


        @JsonProperty("Enable")
        private boolean enable;


        @JsonProperty("Cycle")
        private String cycle;


        @JsonProperty("Since")
        private String since;


        @JsonProperty("Until")
        private String until;
    }


    /**
     * Clase que contiene la respuesta de la validez del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidityDto {


        @JsonProperty("Duration")
        private String duration;


        @JsonProperty("Since")
        private String since;


        @JsonProperty("Until")
        private String until;
    }


    /**
     * Clase que contiene la respuesta del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ConsentDto {


        @JsonProperty("Uuid")
        private String uuid;


        @JsonProperty("CustomerId")
        private String customerId;


        @JsonProperty("CustomerName")
        private String customerName;


        @JsonProperty("CustomerCompanyId")
        private String customerCompanyId;


        @JsonProperty("PartnerId")
        private String partnerId;


        @JsonProperty("PartnerName")
        private String partnerName;


        @JsonProperty("ClientType")
        private String clientType;


        @JsonProperty("Audit")
        private AuditDto audit;


        @JsonProperty("ClientIp")
        private String clientIp;


        @JsonProperty("Authorization")
        private AuthorizationDetailsDto authorization;


        @JsonProperty("Scope")
        private String scope;


        @JsonProperty("FintechName")
        private String fintechName;


        @JsonProperty("Status")
        private String status;


        @JsonProperty("Channel")
        private String channel;


        @JsonProperty("SearchPeriod")
        private SearchPeriodDto searchPeriod;


        @JsonProperty("Recurrence")
        private RecurrenceDto recurrence;


        @JsonProperty("Validity")
        private ValidityDto validity;


        @JsonProperty("Ran207Consent")
        private boolean ran207Consent;


        @JsonProperty("CredentialAsignement")
        private boolean credentialAsignement;


        @JsonProperty("PartnerConfigId")
        private String partnerConfigId;
    }


    /**
     * Clase que contiene la respuesta de auditoria del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuditDto {


        @JsonProperty("CreationDate")
        private String creationDate;


        @JsonProperty("ModificationDate")
        private String modificationDate;


        @JsonProperty("ExpiryDate")
        private String expiryDate;


        @JsonProperty("BrowserInfo")
        private String browserInfo;


        @JsonProperty("AuthorizationTime")
        private String authorizationTime;


        @JsonProperty("HistoricalModification")
        private List<HistoricalModificationDto> historicalModification;
    }


    /**
     * Clase que contiene la respuesta del historial de cambios del consentimiento.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HistoricalModificationDto {


        @JsonProperty("Status")
        private String status;


        @JsonProperty("Date")
        private String date;


        @JsonProperty("Reason")
        private String reason;


        @JsonProperty("ChangeAuthor")
        private String changeAuthor;
    }


    /**
     * Clase que contiene la respuesta de los detalles de la autorizacion.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AuthorizationDetailsDto {


        @JsonProperty("Method")
        private String method;


        @JsonProperty("IdChallenge")
        private String idChallenge;


        @JsonProperty("AuthorizationDate")
        private String authorizationDate;
    }


    /**
     * Clase que contiene el objeto self de la api.
     */
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LinksDto {


        @JsonProperty("self")
        private String self;
    }
}
