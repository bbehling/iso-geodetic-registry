package org.iso.registry.api.registry.registers.gcp.crs;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.hibernate.envers.Audited;
import org.iso.registry.core.model.IdentifiedItem;
import org.iso.registry.core.model.iso19115.extent.EX_Extent;

import de.geoinfoffm.registry.core.model.iso19135.RE_AdditionInformation;
import de.geoinfoffm.registry.core.model.iso19135.RE_ItemClass;
import de.geoinfoffm.registry.core.model.iso19135.RE_Register;

@Access(AccessType.FIELD)
@Audited @Entity 
public abstract class ReferenceSystemItem extends IdentifiedItem
{
	@ManyToOne(cascade = CascadeType.ALL)
	private EX_Extent domainOfValidity;

	public ReferenceSystemItem() {
		super();
	}

	public ReferenceSystemItem(RE_Register register, RE_ItemClass itemClass, String name, 
			String definition, RE_AdditionInformation additionInformation, EX_Extent domainOfValidity) {
		
		super(register, itemClass, name, definition, additionInformation);
		this.domainOfValidity = domainOfValidity;
	}

	public EX_Extent getDomainOfValidity() {
		return domainOfValidity;
	}

	public void setDomainOfValidity(EX_Extent domainOfValidity) {
		this.domainOfValidity = domainOfValidity;
	}

}
