package com.itt.service.config.openapi;

/**
 * Centralized API examples for role management operations.
 * This class contains all the JSON examples used in API documentation
 * to keep them organized and reusable.
 */
public class ApiExamples {

    public static final class RoleManagement {
        
        // Search Request Examples
        public static final String SEARCH_REQUEST = """
            {
              "pagination": {
                "page": 0,
                "size": 10
              },
              "searchFilter": {
                "searchText": "admin",
                "columns": ["name", "description"]
              },
              "columns": [
                {
                  "columnName": "name",
                  "sort": "asc"
                },
                {
                  "columnName": "createdOn",
                  "sort": "desc"
                }
              ]
            }
            """;

        // Response Examples
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
                    "roleType": {
                      "id": 1,
                      "key": "ADMIN_ROLE",
                      "name": "Administrator Role"
                    },
                    "landingPage": {
                      "id": 1,
                      "key": "DASHBOARD",
                      "name": "Dashboard"
                    },
                    "privilegeHierarchy": [
                      {
                        "categoryId": 1,
                        "categoryName": "USER_MANAGEMENT",
                        "categoryKey": "USER_MGMT",
                        "features": [
                          {
                            "featureId": 1,
                            "featureName": "User Operations",
                            "featureKey": "USER_OPS",
                            "privileges": [
                              {
                                "privilegeId": 1,
                                "privilegeName": "Create User",
                                "privilegeKey": "CREATE_USER",
                                "mapCatFeatPrivId": 1,
                                "isSelected": true
                              }
                            ]
                          }
                        ]
                      }
                    ],
                    "skinConfigs": [
                      {
                        "id": 1,
                        "key": "ADMIN_THEME",
                        "name": "Admin Theme"
                      }
                    ],
                    "createdOn": "2024-01-15T10:30:00",
                    "createdById": "SYSTEM",
                    "updatedOn": "2024-01-15T10:30:00",
                    "updatedById": "SYSTEM"
                  }
                ],
                "page": 0,
                "size": 10,
                "totalElements": 25,
                "totalPages": 3,
                "last": false
              }
            }
            """;

        public static final String PRIVILEGE_HIERARCHY_RESPONSE = """
            {
              "success": true,
              "message": "Data retrieved successfully",
              "data": [
                {
                  "categoryId": 1,
                  "categoryName": "USER_MANAGEMENT",
                  "categoryKey": "USER_MGMT",
                  "features": [
                    {
                      "featureId": 1,
                      "featureName": "User Operations",
                      "featureKey": "USER_OPS",
                      "privileges": [
                        {
                          "privilegeId": 1,
                          "privilegeName": "Create User",
                          "privilegeKey": "CREATE_USER",
                          "mapCatFeatPrivId": 1,
                          "isSelected": false
                        },
                        {
                          "privilegeId": 2,
                          "privilegeName": "Update User",
                          "privilegeKey": "UPDATE_USER",
                          "mapCatFeatPrivId": 2,
                          "isSelected": false
                        }
                      ]
                    }
                  ]
                },
                {
                  "categoryId": 2,
                  "categoryName": "ROLE_MANAGEMENT",
                  "categoryKey": "ROLE_MGMT",
                  "features": [
                    {
                      "featureId": 2,
                      "featureName": "Role Operations",
                      "featureKey": "ROLE_OPS",
                      "privileges": [
                        {
                          "privilegeId": 3,
                          "privilegeName": "Create Role",
                          "privilegeKey": "CREATE_ROLE",
                          "mapCatFeatPrivId": 3,
                          "isSelected": false
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """;

        public static final String LANDING_PAGES_RESPONSE = """
            {
              "success": true,
              "message": "Data retrieved successfully",
              "data": [
                {
                  "id": 1,
                  "key": "DASHBOARD",
                  "name": "Dashboard"
                },
                {
                  "id": 2,
                  "key": "REPORTS",
                  "name": "Reports"
                },
                {
                  "id": 3,
                  "key": "ANALYTICS",
                  "name": "Analytics"
                }
              ]
            }
            """;

        public static final String SKIN_GROUPS_RESPONSE = """
            {
              "success": true,
              "message": "Data retrieved successfully",
              "data": [
                {
                  "roleType": "ADMIN",
                  "skins": [
                    {
                      "id": 1,
                      "key": "ADMIN_DARK",
                      "name": "Admin Dark Theme"
                    },
                    {
                      "id": 2,
                      "key": "ADMIN_LIGHT",
                      "name": "Admin Light Theme"
                    }
                  ]
                },
                {
                  "roleType": "USER",
                  "skins": [
                    {
                      "id": 3,
                      "key": "USER_DEFAULT",
                      "name": "User Default Theme"
                    }
                  ]
                }
              ]
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

        public static final String UPDATE_ROLE_REQUEST = """
            {
              "name": "SENIOR_MANAGER",
              "description": "Senior Department Manager Role - Updated",
              "isActive": true,
              "roleTypeConfigId": 2,
              "customLanding": true,
              "landingPageConfigId": 2,
              "skinConfigIds": [4, 5],
              "privilegeIds": [1, 2, 3, 5]
            }
            """;

        public static final String ROLE_CREATED_RESPONSE = """
            {
              "success": true,
              "message": "Role created successfully",
              "data": {
                "id": 1,
                "name": "MANAGER",
                "description": "Department Manager Role",
                "active": true,
                "roleTypeConfigId": 2,
                "landingPageConfigId": 1,
                "createdOn": "2024-01-15T10:30:00",
                "createdById": "SYSTEM",
                "updatedOn": "2024-01-15T10:30:00",
                "updatedById": "SYSTEM"
              }
            }
            """;
    }
}
