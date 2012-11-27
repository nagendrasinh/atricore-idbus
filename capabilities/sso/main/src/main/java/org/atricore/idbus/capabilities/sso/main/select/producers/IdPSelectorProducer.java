package org.atricore.idbus.capabilities.sso.main.select.producers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atricore.idbus.capabilities.sso.main.SSOException;
import org.atricore.idbus.capabilities.sso.main.common.producers.SSOProducer;
import org.atricore.idbus.capabilities.sso.main.select.spi.EntitySelectionContext;
import org.atricore.idbus.capabilities.sso.main.select.spi.EntitySelectorManager;
import org.atricore.idbus.capabilities.sso.main.select.SSOEntitySelectorMediator;
import org.atricore.idbus.capabilities.sso.support.binding.SSOBinding;
import org.atricore.idbus.capabilities.sso.support.core.StatusCode;
import org.atricore.idbus.capabilities.sso.support.core.StatusDetails;
import org.atricore.idbus.common.sso._1_0.protocol.SelectEntityRequestType;
import org.atricore.idbus.common.sso._1_0.protocol.SelectEntityResponseType;
import org.atricore.idbus.kernel.main.federation.metadata.CircleOfTrustMemberDescriptor;
import org.atricore.idbus.kernel.main.federation.metadata.EndpointDescriptor;
import org.atricore.idbus.kernel.main.federation.metadata.EndpointDescriptorImpl;
import org.atricore.idbus.kernel.main.mediation.IdentityMediationFault;
import org.atricore.idbus.kernel.main.mediation.MediationMessageImpl;
import org.atricore.idbus.kernel.main.mediation.MediationState;
import org.atricore.idbus.kernel.main.mediation.camel.AbstractCamelEndpoint;
import org.atricore.idbus.kernel.main.mediation.camel.component.binding.CamelMediationExchange;
import org.atricore.idbus.kernel.main.mediation.camel.component.binding.CamelMediationMessage;
import org.atricore.idbus.kernel.main.util.UUIDGenerator;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class IdPSelectorProducer extends SSOProducer {

    protected UUIDGenerator uuidGenerator = new UUIDGenerator();

    private static final Log logger = LogFactory.getLog(IdPSelectorProducer.class);

    public IdPSelectorProducer(AbstractCamelEndpoint<CamelMediationExchange> endpoint) {
        super(endpoint);
    }

    @Override
    protected void doProcess(CamelMediationExchange exchange) throws Exception {

        CamelMediationMessage in = (CamelMediationMessage) exchange.getIn();
        MediationState state = in.getMessage().getState();
        Object content = in.getMessage().getContent();

        try {

            // ------------------------------------------------------------------------------------------
            // Select Entity
            // ------------------------------------------------------------------------------------------
            if (content instanceof SelectEntityRequestType) {
                SelectEntityRequestType request = (SelectEntityRequestType) content;

                if (logger.isDebugEnabled())
                    logger.debug("Starting IdP selection for " + endpointRef);

                doProcessSelectEntityRequest(exchange, state, request);

            } else {
                throw new IdentityMediationFault(StatusCode.TOP_RESPONDER.getValue(),
                        null,
                        StatusDetails.UNKNOWN_REQUEST.getValue(),
                        content.getClass().getName(),
                        null);
            }



        } catch (Exception e) {
            throw new IdentityMediationFault(StatusCode.TOP_RESPONDER.getValue(),
                    null,
                    StatusDetails.UNKNOWN_REQUEST.getValue(),
                    content.getClass().getName(),
                    e);
        }
    }

    protected void doProcessSelectEntityRequest(CamelMediationExchange exchange, MediationState state, SelectEntityRequestType request) throws SSOException {

        // Do we need to collect more information to make a decision ?!
        SSOEntitySelectorMediator mediator = (SSOEntitySelectorMediator) channel.getIdentityMediator();
        EntitySelectorManager manager = mediator.getSelectorManager();

        // Information can be found:

        // 1. In the request (preferred IdP, requested IdP, etc)
        Map<String, String> attrs = new HashMap<String, String>();

        // TODO : 2. As provider state variables (user IP, user-agent, etc)

        // TODO  : 3. Provided as additional claim ?!

        // Get selection strategies from mediator ...
        EntitySelectionContext ctx = new EntitySelectionContext(getCotManager(), attrs, request);
        CircleOfTrustMemberDescriptor entity = manager.selectEntity(mediator.getPreferredStrategy(), ctx);

        // TODO : Do something with the outcome
        SelectEntityResponseType response = new SelectEntityResponseType();

        response.setEntityId(entity.getId());

        String location = request.getReplyTo();
        if (location == null)
            throw new SSOException("Reply-To attribute is required for select entity request " + request.getID());

        // For now, artifact binding is required.
        EndpointDescriptor ed = new EndpointDescriptorImpl(
                "IDPSelectorResponseEndpoint",
                "EntitySelectorResponse",
                SSOBinding.SSO_ARTIFACT.toString(),
                request.getReplyTo(),
                null);


        // Send SAMLR2 Message back
        CamelMediationMessage out = (CamelMediationMessage) exchange.getOut();

        out.setMessage(new MediationMessageImpl(uuidGenerator.generateId(),
                response,
                "SelectEntityResponse",
                null,
                ed,
                state));

        exchange.setOut(out);



    }
}
