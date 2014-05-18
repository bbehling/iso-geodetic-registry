package org.iso.registry.api;

import static org.springframework.security.acls.domain.BasePermission.READ;
import static org.springframework.security.acls.domain.BasePermission.WRITE;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import de.geoinfoffm.registry.api.RegisterService;
import de.geoinfoffm.registry.core.ProposalSubmittedEvent;
import de.geoinfoffm.registry.core.model.Proposal;
import de.geoinfoffm.registry.core.model.iso19135.RE_Register;
import de.geoinfoffm.registry.persistence.ProposalRepository;

@Transactional
@Component
public class ProposalSubmittedEventListener implements ApplicationListener<ProposalSubmittedEvent> 
{
	@Autowired
	private ProposalRepository proposalRepository;
	
	@Autowired
	private RegisterService registerService;
	
	@Override
	public void onApplicationEvent(ProposalSubmittedEvent event) {
		if (event.isAnnotated(this.getClass())) return;
		
		Proposal proposal = event.getSource();
//		Organization org = orgRepository.findBySubmittingOrganization(proposal.getSponsor());
		
//		Sid orgSid = new PrincipalSid(org.getUuid().toString());
//		proposalRepository.appendAces(proposal, Arrays.asList(ADMINISTRATION, READ, WRITE, DELETE), orgSid, true);
		for (RE_Register register : proposal.getAffectedRegisters()) {
			Sid submitterSid = new GrantedAuthoritySid(registerService.getSubmitterRole(register).getName());
			Sid ownerSid = new GrantedAuthoritySid(registerService.getOwnerRole(register).getName());
			Sid managerSid = new GrantedAuthoritySid(registerService.getManagerRole(register).getName());
			Sid controlBodySid = new GrantedAuthoritySid(registerService.getControlBodyRole(register).getName());

			// TODO Change to READ-only!
			proposalRepository.appendAces(proposal, Arrays.asList(READ, WRITE), submitterSid, true);
			
			proposalRepository.appendAces(proposal, Arrays.asList(READ), ownerSid, true);
			proposalRepository.appendAces(proposal, Arrays.asList(READ, WRITE), managerSid, true);
			proposalRepository.appendAces(proposal, Arrays.asList(READ), controlBodySid, true);
		}
		
		event.annotate(this.getClass());
	}
	
	
}