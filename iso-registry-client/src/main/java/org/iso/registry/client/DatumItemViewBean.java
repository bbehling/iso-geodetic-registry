package org.iso.registry.client;

import java.util.Date;

import org.iso.registry.core.model.crs.AreaItem;
import org.iso.registry.core.model.datum.DatumItem;

import de.geoinfoffm.registry.client.web.RegisterItemViewBean;
import de.geoinfoffm.registry.core.model.Appeal;
import de.geoinfoffm.registry.core.model.Proposal;
import de.geoinfoffm.registry.core.model.SimpleProposal;
import de.geoinfoffm.registry.core.model.Supersession;
import de.geoinfoffm.registry.core.model.iso19135.RE_RegisterItem;

public class DatumItemViewBean extends IdentifiedItemViewBean
{
	private String anchorDefinition;
	private AreaItemViewBean domainOfValidity;
	private Date realizationEpoch;
	private String scope;
	
	public DatumItemViewBean(Appeal appeal) {
		super(appeal);
	}

	public DatumItemViewBean(Proposal proposal) {
		super(proposal);
	}

	public DatumItemViewBean(RE_RegisterItem item, boolean loadDetails) {
		super(item, loadDetails);
	}

	public DatumItemViewBean(RE_RegisterItem item) {
		super(item);
	}

	public DatumItemViewBean(SimpleProposal proposal) {
		super(proposal);
	}

	public DatumItemViewBean(Supersession supersession) {
		super(supersession);
	}

	@Override
	protected void addAdditionalProperties(RE_RegisterItem registerItem) {
		super.addAdditionalProperties(registerItem);
		
		if (!(registerItem instanceof DatumItem)) {
			return;
		}
		
		DatumItem item = (DatumItem)registerItem;

		this.setAnchorDefinition(item.getAnchorDefinition());
		if (item.getDomainOfValidity() != null) {
			this.setDomainOfValidity(new AreaItemViewBean(item.getDomainOfValidity()));
		}
		this.setRealizationEpoch(item.getRealizationEpoch());
		this.setScope(item.getScope());
	}

	public String getAnchorDefinition() {
		return anchorDefinition;
	}

	public void setAnchorDefinition(String anchorDefinition) {
		this.anchorDefinition = anchorDefinition;
	}

	public AreaItemViewBean getDomainOfValidity() {
		return domainOfValidity;
	}

	public void setDomainOfValidity(AreaItemViewBean domainOfValidity) {
		this.domainOfValidity = domainOfValidity;
	}

	public Date getRealizationEpoch() {
		return realizationEpoch;
	}

	public void setRealizationEpoch(Date realizationEpoch) {
		this.realizationEpoch = realizationEpoch;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

}