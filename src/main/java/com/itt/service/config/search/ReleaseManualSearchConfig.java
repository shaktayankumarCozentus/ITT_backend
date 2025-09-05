package com.itt.service.config.search;
import com.itt.service.entity.ReleaseManual;
import com.itt.service.fw.search.SearchableEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class ReleaseManualSearchConfig implements SearchableEntity<ReleaseManual> {

	@Override
	public Set<String> getSearchableFields() {
		return Set.of(
				"id", "noteType", "releaseUserManualName", "fileName","isDeleted","isLatest",
				"dateOfReleaseNote", "uploadedOn", "updatedOn","createdById","updatedById");
	}

	@Override
	public Set<String> getSortableFields() {
		return Set.of(
				"id", "releaseUserManualName", "fileName","isDeleted","isLatest",
				"dateOfReleaseNote", "uploadedOn", "updatedOn", "createdById","updatedById");
	}

	@Override
	public Set<String> getDefaultSearchColumns() {
		return Set.of("id", "noteType");
	}

	@Override
	public List<String> getDefaultSortFields() {
		return List.of("id:asc");
	}

	@Override
	public Class<ReleaseManual> getEntityClass() {
		return ReleaseManual.class;
	}

	@Override
	public Set<String> getFetchJoins() {
		return Set.of();
	}

	@Override
	public boolean shouldUseDistinct() {
		return true;
	}

	@Override
	public Map<String, String> getFieldAliases() {
		return Map.of();
	}
}