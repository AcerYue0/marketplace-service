# Marketplace Service
## Introduction
This project contains two modules:
1. marketplace-core
2. task-service

### Marketplace Core
Handle API calls and data process

APIs (Internal use):  
| Endpoint | Usage |
| --- | --- |
| /fetchList | From keyword JSON, search all keyword and put lowest price into new JSON file |
| /getList | Response JSON created (TODO) |
| (expandable) | ... | 

### Task Service
Handle background task and call APIs in marketplace-core

Crons (Background service):
| Task Name | Cron | Description |
| --- | --- | --- |
| FetchAllNFTCollectionItemsTask | 0 */15 * * * * | Call /fetchList |
| GetNFTCollectionItemsValueTask (TODO) | 0 */30 * * * * | Call /getList |
| (expandable) | ... | ... |

APIs (External usable):
| Endpoint | Usage |
| --- | --- |
| /trigger | Trigger FetchAllNFTCollectionItemsTask manually, and stop cron for one time. |
| /cronEnable | Enable cron manually. |
| /cronDisable | Disable cron manually. |
| /cronStatus | Get current cron status. |
| (expandable) | ... |  

API TODO: /trigger/{taskname} to specific which cron need to be trigger.