package com.itt.service.config.openapi;

/**
 * Centralized API examples for role management operations. This class contains
 * all the JSON examples used in API documentation to keep them organized and
 * reusable.
 */

public class ApiExamples {

	public static final class RoleManagement {
		public static final String SEARCH_REQUEST = """
				    {
				      "pagination": {"page": 0, "size": 10},
				      "searchFilter": {"searchText": "admin"},
				      "columns": [{"columnName": "name", "sort": "asc"}]
				    }
				""";
		public static final String CREATE_ROLE_REQUEST = """
				    {
				      "name": "MANAGER",
				      "description": "Department Manager Role",
				      "isActive": true,
				      "roleTypeConfigId": 2,
				      "customLanding": true,
				      "landingPageConfigId": 1,
				      "skinConfigIds": [3, 4],
				      "privilegeIds": [1, 2, 5]
				    }
				""";
		public static final String ROLES_WITH_PRIVILEGES_RESPONSE = """
				    {
				      "success": true,
				      "message": "Data retrieved successfully",
				      "data": {
				        "content": [
				          {
				            "id": 1,
				            "name": "ADMIN",
				            "description": "System Administrator",
				            "isActive": true,
				            "roleType": {"id": 1, "key": "ADMIN_ROLE", "name": "Administrator Role"},
				            "landingPage": {"id": 1, "key": "DASHBOARD", "name": "Dashboard"},
				            "privilegeHierarchy": [],
				            "skinConfigs": [],
				            "createdOn": "2024-01-15T10:30:00",
				            "createdById": "SYSTEM",
				            "updatedOn": "2024-01-15T10:30:00",
				            "updatedById": "SYSTEM"
				          }
				        ],
				        "page": 0,
				        "size": 10,
				        "totalElements": 1,
				        "totalPages": 1,
				        "last": true
				      }
				    }
				""";
	}

	public static final class UserManagement {
		public static final String SEARCH_USERS_REQUEST = """
				    {
				      "dataTableRequest": {
				        "searchFilter": {
				          "searchText": "john"
				        },
				        "columns": [
				          {
				            "columnName": "fullName",
				            "filter": "",
				            "sort": "asc"
				          }
				        ],
				        "pagination": {
				          "page": 0,
				          "size": 15
				        }
				      },
				      "userRoleFilter": "ACTIVE_OR_NO_ROLE"
				    }
				""";
		public static final String SEARCH_USERS_RESPONSE = """
				    {
				      "success": true,
				      "message": "Users retrieved successfully",
				      "data": {
				        "content": [
				          {
				            "userId": 101,
				            "username": "john.doe",
				            "email": "john.doe@example.com",
				            "roles": ["ADMIN", "USER"],
				            "active": true
				          }
				        ],
				        "page": 0,
				        "size": 20,
				        "totalElements": 1,
				        "totalPages": 1,
				        "last": true
				      }
				    }
				""";
		public static final String USER_COUNT_RESPONSE = """
				    {
				      "success": true,
				      "message": "User count by role activity",
				      "data": {
				        "active": 50,
				        "inactive": 10
				      }
				    }
				""";
		public static final String UPDATE_USER_ASSIGNMENTS_REQUEST = """
				    {
				      "userId": "101",
				      "assignments": ["COMPANY_A", "COMPANY_B"]
				    }
				""";
		public static final String USER_ASSIGNED_COMPANIES_RESPONSE = """
				    {
				      "success": true,
				      "data": [
				        {"companyId": 1, "companyName": "Acme Corp"},
				        {"companyId": 2, "companyName": "Beta Ltd"}
				      ]
				    }
				""";
	}

	public static final class CustomerSubscription {
		public static final String LIST_REQUEST = """
				    {
				      "page": 0,
				      "size": 10,
				      "filters": {"status": "ACTIVE"}
				    }
				""";
		public static final String BULK_UPDATE_REQUEST = """
				    {
				      "subscriptionIds": [1, 2, 3],
				      "tier": "PREMIUM"
				    }
				""";
		public static final String LIST_RESPONSE = """
				    {
				      "success": true,
				      "data": {
				        "content": [
				          {"id": 1, "customer": "Acme Corp", "tier": "PREMIUM", "status": "ACTIVE"}
				        ],
				        "page": 0,
				        "size": 10,
				        "totalElements": 1,
				        "totalPages": 1,
				        "last": true
				      }
				    }
				""";
		public static final String COMPANY_LIST_RESPONSE = """
				    {
				      "success": true,
				      "data": [
				        {"companyId": 1, "companyName": "Acme Corp"},
				        {"companyId": 2, "companyName": "Beta Ltd"}
				      ]
				    }
				""";
	}

	public static final class PetaPetdManagement {
		public static final String LIST_REQUEST = """
				    {
				      "page": 0,
				      "size": 10,
				      "filters": {"type": "PETA"}
				    }
				""";
		public static final String BULK_UPDATE_REQUEST = """
				    {
				      "ids": [1, 2],
				      "tier": "GOLD"
				    }
				""";
		public static final String LIST_RESPONSE = """
				    {
				      "success": true,
				      "data": {
				        "content": [
				          {"id": 1, "type": "PETA", "tier": "GOLD", "status": "ACTIVE"}
				        ],
				        "page": 0,
				        "size": 10,
				        "totalElements": 1,
				        "totalPages": 1,
				        "last": true
				      }
				    }
				""";
		public static final String CONFIGS_RESPONSE = """
				    {
				      "success": true,
				      "data": [
				        {"configId": 1, "configName": "ConfigA"},
				        {"configId": 2, "configName": "ConfigB"}
				      ]
				    }
				""";
	}

	public static final class MasterData {
		public static final String GET_COMPANIES_RESPONSE = """
				    {
				      "success": true,
				      "data": [
				        {"id": 1, "name": "Acme Corp"},
				        {"id": 2, "name": "Beta Ltd"}
				      ]
				    }
				""";
		public static final String GET_CONFIG_BY_KEY_RESPONSE = """
				    {
				      "success": true,
				      "data": [
				        {"key": "SOME_KEY", "value": "Some Value"}
				      ]
				    }
				""";
		public static final String GET_CONFIG_BY_TYPE_RESPONSE = """
				    {
				      "success": true,
				      "data": [
				        {"type": "TYPE_A", "value": "Type Value"}
				      ]
				    }
				""";
	}
}
