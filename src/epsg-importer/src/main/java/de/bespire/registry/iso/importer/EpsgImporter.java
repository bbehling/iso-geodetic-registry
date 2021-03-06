package de.bespire.registry.iso.importer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;

import de.bespire.registry.iso.importer.CoordinateReferenceSystemsImporter.Mode;
import de.geoinfoffm.registry.core.model.iso19135.RE_ItemClass;
import de.geoinfoffm.registry.core.model.iso19135.RE_Register;
import de.geoinfoffm.registry.core.model.iso19135.RE_SubmittingOrganization;
import de.geoinfoffm.registry.core.model.iso19135.SubmittingOrganizationRepository;
import de.geoinfoffm.registry.persistence.ItemClassRepository;
import de.geoinfoffm.registry.persistence.RegisterRepository;

public class EpsgImporter
{
	public static final String GCP_REGISTER_NAME = "Geodetic Codes & Parameters";
	
	private static final Logger logger = LoggerFactory.getLogger(EpsgImporter.class);
	
	private static AnnotationConfigApplicationContext context;
	private static Database db;
	
	public static AnnotationConfigApplicationContext getContext() {
		return context;
	}
	
	public static <T> T getBean(Class<T> beanType) {
		return context.getBean(beanType);
	}
	
	public static Database getDatabase() {
		return db;
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		List<String> argList = Arrays.asList(args);
		
		boolean generateIdentifiers = true;
		
		File source;
		if (argList.isEmpty()) {
			source = new File("C:/Daten/EPSG_v7_6_original.mdb"); 
		}
		else {
			source = new File(args[0]);
		}
		
		if (argList.contains("dontgenerateidentifiers")) {
			generateIdentifiers = false;
		}

		db = DatabaseBuilder.open(source);
		Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
		try {
			Authentication authentication = new PreAuthenticatedAuthenticationToken("SYSTEM", "N/A", Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));
			SecurityContextHolder.getContext().setAuthentication(authentication);

			context = new AnnotationConfigApplicationContext("de.geoinfoffm.registry", "org.iso.registry");
			
//			NamingSystemsImporter namingSystemsImporter = context.getBean(NamingSystemsImporter.class);
//			namingSystemsImporter.setGenerateIdentifiers(generateIdentifiers);

			AreasImporter areasImporter = context.getBean(AreasImporter.class);
			areasImporter.setGenerateIdentifiers(generateIdentifiers);

			UnitsOfMeasurementImporter uomImporter = context.getBean(UnitsOfMeasurementImporter.class);
			uomImporter.setGenerateIdentifiers(generateIdentifiers);

			CoordinateSystemAxesImporter axesImporter = context.getBean(CoordinateSystemAxesImporter.class);
			axesImporter.setNamesTable(db.getTable("Coordinate Axis Name"));
			axesImporter.setGenerateIdentifiers(generateIdentifiers);
			
			CoordinateSystemsImporter csImporter = context.getBean(CoordinateSystemsImporter.class);
			csImporter.setAxisTable(db.getTable("Coordinate Axis"));
			csImporter.setGenerateIdentifiers(generateIdentifiers);

			EllipsoidsImporter ellipsoidsImporter = context.getBean(EllipsoidsImporter.class);
			ellipsoidsImporter.setGenerateIdentifiers(generateIdentifiers);

			PrimeMeridiansImporter pmImporter = context.getBean(PrimeMeridiansImporter.class);
			pmImporter.setGenerateIdentifiers(generateIdentifiers);
			
			DatumsImporter datumsImporter = context.getBean(DatumsImporter.class);
			datumsImporter.setGenerateIdentifiers(generateIdentifiers);
			
			CoordinateReferenceSystemsImporter crsImporter = context.getBean(CoordinateReferenceSystemsImporter.class);
			crsImporter.setGenerateIdentifiers(generateIdentifiers);
			
			AliasesImporter aliasesImporter = context.getBean(AliasesImporter.class);
			aliasesImporter.setGenerateIdentifiers(generateIdentifiers);
			
			OperationMethodsImporter methodsImporter = context.getBean(OperationMethodsImporter.class);
			methodsImporter.setParametersUsageTable(db.getTable("Coordinate_Operation Parameter Usage"));
			methodsImporter.setGenerateIdentifiers(generateIdentifiers);
			
			OperationParametersImporter paramsImporter = context.getBean(OperationParametersImporter.class);
			paramsImporter.setGenerateIdentifiers(generateIdentifiers);
			
			CoordinateOperationsImporter opsImporter = context.getBean(CoordinateOperationsImporter.class);
			opsImporter.setGenerateIdentifiers(generateIdentifiers);
			opsImporter.setParametersUsageTable(db.getTable("Coordinate_Operation Parameter Usage"));
			opsImporter.setParameterValuesTable(db.getTable("Coordinate_Operation Parameter Value"));
			opsImporter.setPathTable(db.getTable("Coordinate_Operation Path"));

			RegistryInitializer initializer = context.getBean(RegistryInitializer.class);
			if (argList.contains("all") || argList.contains("init")) {
				initializer.initializeRegistry();
			}
			
			SubmittingOrganizationRepository suborgRepository = context.getBean(SubmittingOrganizationRepository.class);
			RE_SubmittingOrganization sponsor = suborgRepository.findAll().get(0);
			
			EntityManager em = context.getBean(EntityManager.class);

			RegisterRepository registerRepository = context.getBean(RegisterRepository.class);
			RE_Register register = registerRepository.findByName(GCP_REGISTER_NAME);

			ItemClassRepository itemClassRepository = context.getBean(ItemClassRepository.class);

			if (register == null) {
				throw new RuntimeException(String.format("Registry not initialized: Register '%s' not found", GCP_REGISTER_NAME));
			}
			
			authentication = new PreAuthenticatedAuthenticationToken("SYSTEM", "N/A", 
					Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"), 
							      new SimpleGrantedAuthority("ROLE_SUBMITTER_" + register.getUuid().toString()), 
							      new SimpleGrantedAuthority("ROLE_MANAGER_" + register.getUuid().toString()), 
							      new SimpleGrantedAuthority("ROLE_CONTROLBODY_" + register.getUuid().toString())));
			SecurityContextHolder.getContext().setAuthentication(authentication);
//			RE_ItemClass icNamespace = namingSystemsImporter.getOrCreateItemClass(register, null);
//			if (argList.contains("all") || argList.contains("1") || argList.contains("-ns")) {
//				if (argList.contains("-ns")) {
//					namingSystemsImporter.setLimitToCodes(argList.get(argList.indexOf("-ns") + 1));
//				}
//				Table namingSystemsTable = db.getTable("Naming System");
//				run(namingSystemsImporter, namingSystemsTable, register, sponsor);
//			}				

			RE_ItemClass icUoM = uomImporter.getOrCreateItemClass(register, null);
			if (argList.contains("all") || argList.contains("2") || argList.contains("-uom")) {
				if (argList.contains("-uom")) {
					uomImporter.setLimitToCodes(argList.get(argList.indexOf("-uom") + 1));
				}
				Table uomTable = db.getTable("Unit of Measure");
				run(uomImporter, uomTable, register, sponsor);
			}				
				
			RE_ItemClass icAxis = axesImporter.getOrCreateItemClass(register, null);
			if (argList.contains("all") || argList.contains("3") || argList.contains("-axis")) {
				if (argList.contains("-axis")) {
					axesImporter.setLimitToCodes(argList.get(argList.indexOf("-axis") + 1));
				}
				Table axesTable = db.getTable("Coordinate Axis");
				run(axesImporter, axesTable, register, sponsor);
			}				

			RE_ItemClass icEllipsoid = ellipsoidsImporter.getOrCreateItemClass(register, null);
			if (argList.contains("all") || argList.contains("5") || argList.contains("-ellipsoid")) {
				if (argList.contains("-ellipsoid")) {
					ellipsoidsImporter.setLimitToCodes(argList.get(argList.indexOf("-ellipsoid") + 1));
				}
				Table elTable = db.getTable("Ellipsoid");
				run(ellipsoidsImporter, elTable, register, sponsor);
			}				
	
			RE_ItemClass icPrime = pmImporter.getOrCreateItemClass(register, null);
			if (argList.contains("all") || argList.contains("6") || argList.contains("-pm")) {
				if (argList.contains("-pm")) {
					pmImporter.setLimitToCodes(argList.get(argList.indexOf("-pm") + 1));
				}
				Table pmTable = db.getTable("Prime Meridian");
				run(pmImporter, pmTable, register, sponsor);
			}				

			for (String type : new String[] { "ELLIPSOIDAL", "SPHERICAL", "CARTESIAN", "VERTICAL" }) {
				RE_ItemClass icCS = csImporter.getOrCreateItemClass(register, type);
			}
			if (argList.contains("all") || argList.contains("4") || argList.contains("-cs")) {
				if (argList.contains("-cs")) {
					csImporter.setLimitToCodes(argList.get(argList.indexOf("-cs") + 1));
				}
				Table csTable = db.getTable("Coordinate System");
				run(csImporter, csTable, register, sponsor);
			}				

			RE_ItemClass icArea = areasImporter.getOrCreateItemClass(register, null);
			if (argList.contains("all") || argList.contains("7") || argList.contains("-area") || argList.contains("-allareas")) {
				if (argList.contains("-area")) {
					areasImporter.setLimitToCodes(argList.get(argList.indexOf("-area") + 1));
				}
				Table areasTable = db.getTable("Area");
				run(areasImporter, areasTable, register, sponsor);
			}				
	
			for (String type : new String[] { "GEODETIC", "ENGINEERING", "VERTICAL" }) {
 				RE_ItemClass icDatum = datumsImporter.getOrCreateItemClass(register, type);
			}
			if (argList.contains("all") || argList.contains("8") || argList.contains("-datum")) {
				if (argList.contains("-datum")) {
					datumsImporter.setLimitToCodes(argList.get(argList.indexOf("-datum") + 1));
				}
				Table datumsTable = db.getTable("Datum");
				run(datumsImporter, datumsTable, register, sponsor);
			}				
				
			for (String type : new String[] { "GEOCENTRIC", "ENGINEERING", "VERTICAL", "COMPOUND", "PROJECTED" }) {
				RE_ItemClass icCRS = crsImporter.getOrCreateItemClass(register, type);
			}
			if (argList.contains("all") || argList.contains("9") || argList.contains("-crs")) {
				if (argList.contains("-crs")) {
					crsImporter.setLimitToCodes(argList.get(argList.indexOf("-crs") + 1));
				}
				crsImporter.setMode(Mode.SINGLE);
				Table crsTable = db.getTable("Coordinate Reference System");
				run(crsImporter, crsTable, register, sponsor);
			}				

			RE_ItemClass icParam = paramsImporter.getOrCreateItemClass(register, null);
			if (argList.contains("all") || argList.contains("11") || argList.contains("-param")) {
				if (argList.contains("-param")) {
					paramsImporter.setLimitToCodes(argList.get(argList.indexOf("-param") + 1));
				}
				Table opTable = db.getTable("Coordinate_Operation Parameter");
				run(paramsImporter, opTable, register, sponsor);
			}				

			RE_ItemClass icMethod = methodsImporter.getOrCreateItemClass(register, null);
			if (argList.contains("all") || argList.contains("12") || argList.contains("-method")) {
				if (argList.contains("-method")) {
					methodsImporter.setLimitToCodes(argList.get(argList.indexOf("-method") + 1));
				}
				Table methodsTable = db.getTable("Coordinate_Operation Method");
				run(methodsImporter, methodsTable, register, sponsor);
			}				

			RE_ItemClass icOperation = opsImporter.getOrCreateItemClass(register, null);
			if (argList.contains("all") || argList.contains("13") || argList.contains("-op")) {
				if (argList.contains("-op")) {
					opsImporter.setLimitToCodes(argList.get(argList.indexOf("-op") + 1));
				}
				opsImporter.setMode(CoordinateOperationsImporter.Mode.CONVERSION);
				Table coopTable = db.getTable("Coordinate_Operation");
				run(opsImporter, coopTable, register, sponsor);
			}			

			if (argList.contains("all") || argList.contains("9") || argList.contains("-crs")) {
				crsImporter.setMode(Mode.DERIVED_PROJECTED);
				Table crsTable = db.getTable("Coordinate Reference System");
				run(crsImporter, crsTable, register, sponsor);
			}				

			if (argList.contains("all") || argList.contains("9") || argList.contains("-crs")) {
				crsImporter.setMode(Mode.COMPOUND);
				Table crsTable = db.getTable("Coordinate Reference System");
				run(crsImporter, crsTable, register, sponsor);
			}				

			if (argList.contains("all") || argList.contains("13") || argList.contains("-op")) {
				if (argList.contains("-op")) {
					opsImporter.setLimitToCodes(argList.get(argList.indexOf("-op") + 1));
				}
				opsImporter.setMode(CoordinateOperationsImporter.Mode.TRANSFORMATION);
				Table coopTable = db.getTable("Coordinate_Operation");
				run(opsImporter, coopTable, register, sponsor);
			}			

			if (argList.contains("all") || argList.contains("13") || argList.contains("-op")) {
				if (argList.contains("-op")) {
					opsImporter.setLimitToCodes(argList.get(argList.indexOf("-op") + 1));
				}
				opsImporter.setMode(CoordinateOperationsImporter.Mode.CONCATENATED);
				Table coopTable = db.getTable("Coordinate_Operation");
				run(opsImporter, coopTable, register, sponsor);
			}			

			if (argList.contains("all") || argList.contains("9") || argList.contains("-crs")) {
				Table crsTable = db.getTable("Coordinate Reference System");
				fixReferences(crsImporter, register, sponsor, crsTable);
			}				

			if (argList.contains("all") || argList.contains("10") || argList.contains("-alias") || argList.contains("-allaliases")) {
				if (argList.contains("-alias")) {
					aliasesImporter.setLimitToCodes(argList.get(argList.indexOf("-alias") + 1));
				}
				Table aliasTable = db.getTable("Alias");
				run(aliasesImporter, aliasTable, register, sponsor);
			}
			
			register = registerRepository.findOne(register.getUuid());
			
			Query q = em.createQuery("SELECT c FROM RE_ItemClass c JOIN FETCH c.registers WHERE c.name = :name");
			q.setParameter("name", icArea.getName());
			icArea = (RE_ItemClass)q.getSingleResult();
			
			try {
				register.getContainedItemClasses().remove(icArea);
				icArea.getRegisters().remove(register);
				itemClassRepository.save(icArea);
				registerRepository.save(register);
			}
			catch (Throwable t) {
				logger.error(t.getMessage(), t);
			}
		}
		finally {
			db.close();
			if (context != null) {
				context.close();
			}
		}		
	}
	
	private static void run(AbstractImporter importer, Table table, RE_Register register, RE_SubmittingOrganization sponsor) throws IOException {
		logger.info(".");
		logger.info(".");
		logger.info(".");
		logger.info(".");
		logger.info(".");
		logger.info(".");
		logger.info(".");
		logger.info(".");
		logger.info(".");
		logger.info(".");
		
//		importer.clearAway();

		Cursor cursor = table.getDefaultCursor();
		
		int rowCount = table.getRowCount();
		int i = 1;
		if (importer.isLimited()) {
			logger.info(".");
			logger.info(".");
			logger.info(".");
			logger.info("======================================================================");
			logger.info("> Happily importing (limited) rows from MDB table {}...", table.getName());
			logger.info("======================================================================");
			logger.info(".");
			logger.info(".");
			logger.info(".");
			importer.importRows(cursor, -1, sponsor, register);
		}
		else {
			do {
				logger.info(".");
				logger.info(".");
				logger.info(".");
				logger.info("======================================================================");
				logger.info("> Happily importing {} rows from MDB table {}...", table.getRowCount(), table.getName());
				logger.info(">>> Will now import rows #{} to #{}...", new Object[] { i, (i + 99 < rowCount) ? i + 99 : rowCount });
				logger.info("======================================================================");
				logger.info(".");
				logger.info(".");
				logger.info(".");
				importer.importRows(cursor, 100, sponsor, register);
				i += 100;
			} while (i <= rowCount);
		}
		
		fixReferences(importer, register, sponsor, table);
		
		for (String itemClass : importer.getMissingCodes().keySet()) {
			StringBuilder listBuilder = new StringBuilder();
			for (Integer code : importer.getMissingCodes().get(itemClass)) {
				if (listBuilder.length() > 0) {
					listBuilder.append(",");
				}
				listBuilder.append(code);
			}
			logger.info("!!! Missing codes of item class {}: {}", itemClass, listBuilder.toString());
		}
	
//		for (int i = 0; i < table.getRowCount(); i++) {
//			Row row = table.getNextRow();
//
//			logger.info("> Importing row #{}...", i + 1);
//			RE_ItemClass itemClass = importer.getOrCreateItemClass(register, row); 
//			if (itemClass == null) { 
//				new Object();
//			}
//			importer.importRow(row, itemClass, sponsor, register);
//
////			if (i > 200) break;
//		}
	}

	private static void fixReferences(AbstractImporter importer, RE_Register register,
			RE_SubmittingOrganization sponsor, Table table) throws IOException {
		int i;
		if (importer.mustFixReferences()) {
			Cursor cursor = table.getDefaultCursor();
			int rowCount = table.getRowCount();

			cursor.beforeFirst();

			i = 1;
			do {
				logger.info(".");
				logger.info(".");
				logger.info(".");
				logger.info("======================================================================");
				logger.info("> Happily fixing references...");
				logger.info(">>> Will now fix rows #{} to #{}...", new Object[] { i, (i + 99 < rowCount) ? i + 99 : rowCount });
				logger.info("======================================================================");
				logger.info(".");
				logger.info(".");
				logger.info(".");
				importer.fixReferences(cursor, 100, sponsor, register);
				i += 100;
			} while (i <= rowCount);
		}
	}

}
