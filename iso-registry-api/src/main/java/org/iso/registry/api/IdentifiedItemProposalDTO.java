package org.iso.registry.api;

import java.util.ArrayList;
import java.util.List;

import org.iso.registry.api.registry.registers.gcp.AliasDTO;
import org.iso.registry.core.model.Alias;
import org.iso.registry.core.model.IdentifiedItem;
import org.isotc211.iso19135.RE_RegisterItem_Type;

import de.geoinfoffm.registry.api.RegisterItemProposalDTO;
import de.geoinfoffm.registry.core.model.Proposal;
import de.geoinfoffm.registry.core.model.iso19135.RE_RegisterItem;
import de.geoinfoffm.registry.core.model.iso19135.RE_SubmittingOrganization;
import de.geoinfoffm.registry.soap.Addition_Type;

public class IdentifiedItemProposalDTO extends RegisterItemProposalDTO
{
//	private String nameCodespace;
//	private String nameCodespaceVersion;
//	private CI_Citation nameCodespaceCitation;
	
	private List<AliasDTO> aliases;
	private String remarks;

	
	public IdentifiedItemProposalDTO() {
		super();
	}
	
	public IdentifiedItemProposalDTO(IdentifiedItem item) {
		super(item);
	}
	
	public IdentifiedItemProposalDTO(Addition_Type proposal, RE_SubmittingOrganization sponsor) {
		super(proposal, sponsor);
	}
	
	public IdentifiedItemProposalDTO(Proposal proposal) {
		super(proposal);
	}

	public IdentifiedItemProposalDTO(RE_RegisterItem_Type item, RE_SubmittingOrganization sponsor) {
		super(item, sponsor);
	}

	public List<AliasDTO> getAliases() {
		return aliases;
	}

	public void setAliases(List<AliasDTO> aliases) {
		this.aliases = aliases;
	}
	
	public void addAlias(AliasDTO alias) {
		if (this.aliases == null) {
			this.aliases = new ArrayList<AliasDTO>();
		}
		this.aliases.add(alias);
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}
	
//	@Override
//	public void setAdditionalValues(RE_RegisterItem registerItem, EntityManager entityManager) {
//		super.setAdditionalValues(registerItem, entityManager);
//		
//		if (registerItem instanceof IdentifiedItem) {
//			IdentifiedItem item = (IdentifiedItem)registerItem;
//
//			item.setAliases(aliases);
//		}
//	}

	@Override
	public void loadAdditionalValues(RE_RegisterItem registerItem) {
		super.loadAdditionalValues(registerItem);
		
		if (registerItem instanceof IdentifiedItem) {
			IdentifiedItem item = (IdentifiedItem)registerItem;

			for (Alias alias : item.getAliases()) {
				this.addAlias(new AliasDTO(alias));
			}
			this.setRemarks(item.getRemarks());
		}
	}

//	public String getNameCodespace() {
//		return nameCodespace;
//	}
//
//	public void setNameCodespace(String nameCodespace) {
//		this.nameCodespace = nameCodespace;
//	}
//
//	public String getNameCodespaceVersion() {
//		return nameCodespaceVersion;
//	}
//
//	public void setNameCodespaceVersion(String nameCodespaceVersion) {
//		this.nameCodespaceVersion = nameCodespaceVersion;
//	}
}
