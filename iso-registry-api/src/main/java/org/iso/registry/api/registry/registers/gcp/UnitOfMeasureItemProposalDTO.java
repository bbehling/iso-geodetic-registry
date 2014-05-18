package org.iso.registry.api.registry.registers.gcp;

import javax.persistence.EntityManager;

import org.iso.registry.api.IdentifiedItemProposalDTO;
import org.iso.registry.core.model.UnitOfMeasureItem;
import org.iso.registry.core.model.iso19103.MeasureType;

import de.geoinfoffm.registry.core.model.iso19135.RE_RegisterItem;

public class UnitOfMeasureItemProposalDTO extends IdentifiedItemProposalDTO
{
	private MeasureType measureType;
	private String symbol;

	private UnitOfMeasureItemProposalDTO standardUnit;
	private Double offsetToStandardUnit;
	private Double scaleToStandardUnitNumerator;
	private Double scaleToStandardUnitDenominator;

	
	public UnitOfMeasureItemProposalDTO() { }

	public UnitOfMeasureItemProposalDTO(UnitOfMeasureItem uom) {
		super(uom);
	}

	public MeasureType getMeasureType() {
		return measureType;
	}

	public void setMeasureType(MeasureType measureType) {
		this.measureType = measureType;
	}

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public UnitOfMeasureItemProposalDTO getStandardUnit() {
		return standardUnit;
	}

	public void setStandardUnit(UnitOfMeasureItemProposalDTO standardUnit) {
		this.standardUnit = standardUnit;
	}

	public Double getOffsetToStandardUnit() {
		return offsetToStandardUnit;
	}

	public void setOffsetToStandardUnit(Double offsetToStandardUnit) {
		this.offsetToStandardUnit = offsetToStandardUnit;
	}

	public Double getScaleToStandardUnitNumerator() {
		return scaleToStandardUnitNumerator;
	}

	public void setScaleToStandardUnitNumerator(Double scaleToStandardUnitNumerator) {
		this.scaleToStandardUnitNumerator = scaleToStandardUnitNumerator;
	}

	public Double getScaleToStandardUnitDenominator() {
		return scaleToStandardUnitDenominator;
	}

	public void setScaleToStandardUnitDenominator(Double scaleToStandardUnitDenominator) {
		this.scaleToStandardUnitDenominator = scaleToStandardUnitDenominator;
	}

	@Override
	public void setAdditionalValues(RE_RegisterItem registerItem, EntityManager entityManager) {
		super.setAdditionalValues(registerItem, entityManager);
		
		if (registerItem instanceof UnitOfMeasureItem) {
			UnitOfMeasureItem uom = (UnitOfMeasureItem)registerItem;
			
			uom.setMeasureType(this.getMeasureType());
			uom.setSymbol(this.getSymbol());
			
			if (this.getStandardUnit() != null) {
				UnitOfMeasureItem standardUnit = entityManager.find(UnitOfMeasureItem.class, this.getStandardUnit().getReferencedItemUuid());
				uom.setStandardUnit(standardUnit);				
			}
			
			uom.setOffsetToStandardUnit(this.getOffsetToStandardUnit());
			uom.setScaleToStandardUnitNumerator(this.getScaleToStandardUnitNumerator());
			uom.setScaleToStandardUnitDenominator(this.getScaleToStandardUnitDenominator());
		}
	}

	@Override
	public void loadAdditionalValues(RE_RegisterItem registerItem) {
		super.loadAdditionalValues(registerItem);
		
		if (registerItem instanceof UnitOfMeasureItem) {
			UnitOfMeasureItem uom = (UnitOfMeasureItem)registerItem;
		
			this.setMeasureType(uom.getMeasureType());
			this.setSymbol(uom.getSymbol());
			if (uom.getStandardUnit() != null) {
				this.setStandardUnit(new UnitOfMeasureItemProposalDTO(uom.getStandardUnit()));
			}
			this.setOffsetToStandardUnit(uom.getOffsetToStandardUnit());
			this.setScaleToStandardUnitNumerator(uom.getScaleToStandardUnitNumerator());
			this.setScaleToStandardUnitDenominator(uom.getScaleToStandardUnitDenominator());
		}
	}

	
}