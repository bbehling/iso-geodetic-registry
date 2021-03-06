package de.bespire.registry.iso.importer;

import java.io.IOException;

import org.iso.registry.api.registry.registers.gcp.ExtentDTO;
import org.iso.registry.api.registry.registers.gcp.datum.DatumItemProposalDTO;
import org.iso.registry.api.registry.registers.gcp.datum.EllipsoidItemProposalDTO;
import org.iso.registry.api.registry.registers.gcp.datum.PrimeMeridianItemProposalDTO;
import org.iso.registry.core.model.crs.AreaItem;
import org.iso.registry.core.model.crs.AreaItemRepository;
import org.iso.registry.core.model.datum.DatumItemRepository;
import org.iso.registry.core.model.datum.EllipsoidItem;
import org.iso.registry.core.model.datum.EllipsoidItemRepository;
import org.iso.registry.core.model.datum.PrimeMeridianItem;
import org.iso.registry.core.model.datum.PrimeMeridianItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.healthmarketscience.jackcess.Row;

import de.geoinfoffm.registry.core.UnauthorizedException;
import de.geoinfoffm.registry.core.model.Addition;
import de.geoinfoffm.registry.core.model.iso19135.InvalidProposalException;
import de.geoinfoffm.registry.core.model.iso19135.RE_ItemClass;
import de.geoinfoffm.registry.core.model.iso19135.RE_Register;
import de.geoinfoffm.registry.core.model.iso19135.RE_SubmittingOrganization;

@Component
public class DatumsImporter extends AbstractImporter
{
	private static final Logger logger = LoggerFactory.getLogger(DatumsImporter.class);

	public static final String DATUM_CODE = "DATUM_CODE";
	public static final String DATUM_NAME = "DATUM_NAME";
	public static final String DATUM_TYPE = "DATUM_TYPE";
	public static final String ORIGIN_DESCRIPTION = "ORIGIN_DESCRIPTION";
	public static final String REALIZATION_EPOCH = "REALIZATION_EPOCH";
	public static final String ELLIPSOID_CODE = "ELLIPSOID_CODE";
	public static final String PRIME_MERIDIAN_CODE = "PRIME_MERIDIAN_CODE";
	public static final String AREA_OF_USE_CODE = "AREA_OF_USE_CODE";
	public static final String DATUM_SCOPE = "DATUM_SCOPE";

	@Autowired
	private EllipsoidItemRepository ellipsoidRepository;
	
	@Autowired
	private PrimeMeridianItemRepository pmRepository;
	
	@Autowired
	private AreaItemRepository areaRepository;
	
	@Autowired
	private DatumItemRepository datumRepository;
	
	private RE_ItemClass icGeodetic;

	private RE_ItemClass icVertical;

	private RE_ItemClass icEngineering;

	@Override
	@Transactional
	protected void importRow(Row row, RE_ItemClass itemClass, RE_SubmittingOrganization sponsor, RE_Register register) throws IOException, UnauthorizedException {
		DatumItemProposalDTO proposal = new DatumItemProposalDTO();
		proposal.setItemClassUuid(itemClass.getUuid());
		proposal.setSponsorUuid(sponsor.getUuid());
		proposal.setTargetRegisterUuid(register.getUuid());

		fillProposalRelatedFields(proposal, row, codeProperty());
		
		Integer epsgCode = (Integer)row.get(DATUM_CODE);
//		proposal.setIdentifier((Integer)row.get(DATUM_CODE));
		proposal.setName((String)row.get(DATUM_NAME));
		proposal.setAnchorDefinition((String)row.get(ORIGIN_DESCRIPTION));
		proposal.setScope((String)row.get(DATUM_SCOPE));
//		proposal.setRealizationEpoch((String)row.get(REALIZATION_EPOCH));
		proposal.setCoordinateReferenceEpoch((String)row.get(REALIZATION_EPOCH));
		
		Integer elCode = (Integer)row.get(ELLIPSOID_CODE);
		Integer pmCode = (Integer)row.get(PRIME_MERIDIAN_CODE);
		Integer areaCode = (Integer)row.get(AREA_OF_USE_CODE);

		if (elCode != null) {
//			EllipsoidItem ellipsoid = ellipsoidRepository.findOne(findMappedCode("Ellipsoid", elCode));
			EllipsoidItem ellipsoid = findMappedEntity("Ellipsoid", elCode, EllipsoidItem.class);
			proposal.setEllipsoid(new EllipsoidItemProposalDTO(ellipsoid));
		}
		if (pmCode != null) {
			PrimeMeridianItem primeMeridian = findMappedEntity("PrimeMeridian", pmCode, PrimeMeridianItem.class);
			proposal.setPrimeMeridian(new PrimeMeridianItemProposalDTO(primeMeridian));

		}
		if (areaCode != null) {
			AreaItem area = findMappedEntity("Area", areaCode, AreaItem.class);
			if (area != null) {
				ExtentDTO extent = new ExtentDTO();
				extent.getGeographicBoundingBoxes().add(area.getBoundingBox());
				extent.setDescription(area.getDescription());
				proposal.setDomainOfValidity(extent);
			}
		}
		
		proposal.setRemarks((String)row.get(REMARKS));
		addInformationSource(proposal, (String)row.get(INFORMATION_SOURCE));
		proposal.setDataSource((String)row.get(DATA_SOURCE));
		
		try {
			Addition ai = proposalService.createAdditionProposal(proposal);
			proposalService.submitProposal(ai);
			
			String decisionEvent = AbstractImporter.IMPORT_SOURCE;
			acceptProposal(ai, decisionEvent);

			logger.info(">> Imported '{}'...", proposal.getName());

			addMapping(ai.getItem().getItemClass().getName(), epsgCode, ai.getItem().getUuid());
		}
		catch (InvalidProposalException e) {
			logger.error(e.getMessage(), e);
		}

	}

	@Transactional
	public RE_ItemClass getOrCreateItemClass(RE_Register register, String type) {
		if (type.equalsIgnoreCase("GEODETIC")) {
			if (icGeodetic == null) {
				icGeodetic = itemClassRepository.findByName("GeodeticDatum");
				if (icGeodetic == null) {
					this.addItemClass("GeodeticDatum", register);
					icGeodetic = itemClassRepository.findByName("GeodeticDatum");
				}
			}

			return icGeodetic;
		}
		else if (type.equalsIgnoreCase("ENGINEERING")) {
			if (icEngineering == null) {
				icEngineering = itemClassRepository.findByName("EngineeringDatum");
				if (icEngineering == null) {
					this.addItemClass("EngineeringDatum", register);
					icEngineering = itemClassRepository.findByName("EngineeringDatum");
				}
			}

			return icEngineering;
		}
		else if (type.equalsIgnoreCase("VERTICAL")) {
			if (icVertical == null) {
				icVertical = itemClassRepository.findByName("VerticalDatum");
				if (icVertical == null) {
					this.addItemClass("VerticalDatum", register);
					icVertical = itemClassRepository.findByName("VerticalDatum");
				}
			}

			return icVertical;
		}
		else {
			return null;
		}		
	}

	@Override
	@Transactional
	public RE_ItemClass getOrCreateItemClass(RE_Register register, Row row) {
		String type = (String)row.get(DATUM_TYPE);
		
		return getOrCreateItemClass(register, type);
	}

	@Override
	@Transactional
	protected void clearAway() {
		datumRepository.deleteAll();
	}

	@Override
	protected String codeProperty() {
		return DATUM_CODE;
	}

}
