# Connector Template

A queue driven integration with the RealTimeRental PMS API <https://url-to-api-documentation>. The service will long poll an SQS queue and process messages. Upon completion of a job, the service will send a message to the inbound queue for the `Legacy Sync Connector` in order to trigger a sync between the new data and the legacy portal.

This application has a Terraform dependency on the `Legacy Sync Connector`. It must use `Legacy Sync Connector`'s remote state in order to get access to the connector's inbound queue so that it can send it messages to trigger runs.

TODOs

- Create ECR repository for connector in terraform-sharedservices repository
- Update places in terraform that have `<name>` to be the new connector name
  - ./src/terraform/src/main.tf
  - ./src/terraform/modules/iam/main.tf
  - ./src/terraform/README.md
- Update Circle CI config and scripts where `<name>` is a placeholder
  - ./circleci/config.yml
  - ./circleci/speculative_plan.ps1
- Update `./tools/deploy.ps1` to have correct name for CircleCI deploy project
- Update POM.xml to have correct artifactId name
- Update `./src/main/resources/` files to replace `<name>` placeholder
  - bootstrap.yml
  - logback-spring.xml
  - check all application.yml files
- Implement Retrofit API interfaces in `./src/main/java/com.safely.batch.connector/client`
- Implement any PMS response objects in `./src/main/java/com.safely.batch.connector/pms`
- Update the `LoadPms*Service` files to meet API needs in `./src/main/java/com.safely.batch.connector/components`
- Update the `ConvertPms*Service` files to meet API needs in `./src/main/java/com.safely.batch.connector/components`
- Resolve all code todos in the java code
- Clean up this readme, update name and remove todo

## Terraform / AWS

| path            | description                                |
| --------------- | ------------------------------------------ |
| ./src/terraform | root directory for terraform configuration |

## Application Code

| path       | description                  |
| ---------- | ---------------------------- |
| ./src/main | root directory for java code |

## Tools

| path               | description                                                         |
| ------------------ | ------------------------------------------------------------------- |
| ./tools/deploy.ps1 | calls CircleCI pipeline to deploy the app `help ./tools/deploy.ps1` |
| ./mvnw             | Maven wrapper                                                       |

## IntelliJ Run/Debug Configuration

1. Active Profiles: `local`
2. Environment Variables: `ISLOCAL=true;SSM_PREFIX=<placeholder>`

    | name       | value                 |
    | ---------- | --------------------- |
    | IS_LOCAL   | true                  |
    | SSM_PREFIX | from terraform output |

3. Override parameters:

    | name          | value |
    | ------------- | ----- |
    | user.timezone | UTC   |

## Local Development Workflow

1. Create feature branch in git `git checkout -b <branchname>`
2. Run Terraform `plan`/`apply` in `./src/terraform/src` to create branch resources in the development account with the branch name as the prefix
3. Code
4. Run or debug the application
5. Push branch and create PR
6. Clean up branch resources by running Terraform `destroy` in `./src/terraform/src`
