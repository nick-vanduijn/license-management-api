/**
 * Domain entities package with shared Hibernate filter definitions.
 */
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = String.class))
package com.licensing.domain;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
