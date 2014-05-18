package org.iso.registry.core.model;

import org.springframework.stereotype.Repository;

import de.geoinfoffm.registry.core.EntityRepository;

@Repository
public interface UnitOfMeasureItemRepository extends EntityRepository<UnitOfMeasureItem>
{
	UnitOfMeasureItem findByCode(Integer code);
}