# Disable Editing plugin for 1C:EDT

> Russian description goes after English

> Русское описание идет после английского

Plugin for 1C:Enterprise Development Tools allows to disable editing of any 1C metadata objects in UI editors with reach settings.

Historically developers used supplier's settings to enable editing of some metadata objects and the rest of objects keep disabled (aka supplied by Configuration vendor).

New IDE, called 1C:EDT, now allows develop application more flexible and use git for supplier-master branch workflow, initial supplier's settings now can be still used but give more tricks than more help, so most developers just remove them from repository.

Any way, developers may want to protect themselves or their juniors from accidental editing some object that they didn't want to edit while exploring reach Configurations.

Disable Editing plugin allows to set up some rules to make read-only mode in all UI editors of 1C:EDT.


## Install

Select menu: `Help -> Install New Software...` past in p2-repository URL [https://marmyshev.gitlab.io/edt-editing/update](https://marmyshev.gitlab.io/edt-editing/update)

Select installable feature `Disable Editing plugin for 1C:EDT`


## Main features

Set up rules to **Disable** editing (read-only mode) in editors:

- specify subsystems which content objects are disabled to edit, hierarchy of subsystems is supported
- specify project relative full path or folders which content is disabled to edit
- specify full qualified names of 1C:Enterprise metadata objects (that stores in single file) that should be disabled in editors

Also allows to set up  **Enable**  exceptions from disable rules:

- by subsystem
- by project relative path to file or to folder
- by full qualified name


> Note! This plugin is not disable editing files via file system!

**Attantion!**  All disabled object will be skipped from project validation by 1C:EDT.


## Set up your project rules

Place Yaml file in your project settings folder: `YourProjectName/.settings/editing.yml` with content:


```yaml
# section allows to disable editing in UI
disable:
   # List of subsystems which content should be disabled
   subsystem:
     - DisabledSubsystem1
     - DisabledSubsystem2
   # List of project relative path to file or to folder 
   path:
     - src/CommonModules/LockedOnlyModule/Module.bsl
   
   # Full qualified  name of top objects (resources)
   fullname:
     - Catalog.Products.Form.ItemForm.Form
     
# section allows to make some exceptions from disable rule
enable:
  subsystem:
    - EnabledSubsystem
  path:
    - src/Catalogs/Enabled

```

##  Demo example

Open 1C:EDT and import project from this repository [EditingDemoConfig](EditingDemoConfig) into workspace.

[Demo settings here](EditingDemoConfig/.settings/editing.yml).

Open metadata objects to check out disabling and enabling features!


# Плагин запрета редактирования для 1C:EDT

Плагин для 1C:Enterprise Development Tools позволяет заблокировать редактирование любого объекта метаданных 1С в редактора в интерфейсе с широкими настройками.

Исторически, разработчики использовали настройки поддержки поставщика чтобы разрешить редактирование некоторых объектов метаданных, а остальные объекты оставить заблокированными (типа на поддержке поставщика Конфигурации).

Новая IDE, называема 1C:EDT теперь позволяет разрабатывать приложения более гибко в т.ч. используя Git для процесса ветвления supplier-master, начальные настройки поставки можно при этом использовать, но они добавляют сложности больше чем помощи. Поэтому большинство разработчиков просто удаляют их из репозитория.

В любом случае, разрабочики могут желать защитить себя или более молодых коллег от случайного редактирования некоторых объектов, которые они бы не хотели редактировать в процесс изучения больших Конфигураций.

Плагин запрета редактирования позволяет настроить некоторые правила чтобы включить режим Только-просмотр во всех редакторах интерфейса 1C:EDT.


## Установка

Выбеите меню: `Help -> Install New Software...` вставьте адрес p2-репозитория [https://marmyshev.gitlab.io/edt-editing/update](https://marmyshev.gitlab.io/edt-editing/update)

Выберите фичу для установки `Disable Editing plugin for 1C:EDT`


## Основные возможности

Настройка правил блокировки ( **Disable** ) редактирования (режим Только-просмотр) в редакторах:

- Указание подсистем, состав объектов которых  заблокирован для редактирования, иерархия подсистем поддерживается
- Указание относительно проекта полного пути к файлу или к  каталогу, контент которых заблокирован для редактирования
- Указание полного квалифицированного имени метаданного 1С:Предприятия (хранящиеся в отдельных файлах), которые должны быть заблокированы в редакторах

Так же позволяет настроить правила исключения ( **Enable** )  для разрешения редактирования:

- по подсистеме
- по пути к файлу или каталогу относительно проекта
- по полному квалифицированному имени


> Примечание! Этот плагин не позволяет заблокировать редактирование файлов через файловую систему!

**Внимание!**  Все заблокированные объекты будут исключены из проверки (валидации) по проекту в 1C:EDT.


##  Настройка правил по проекту


Разместите Ямл файл с настройками в каталоге вашего проектта: `YourProjectName/.settings/editing.yml` с содержанием:


```yaml
# секция позволяет заблокировать редактирование в UI
disable:
   # Список подсистем, состав объектов которых должен быть заблокирован
   subsystem:
     - DisabledSubsystem1
     - DisabledSubsystem2
   # Список путей относительно проекта к файлам или каталогам
   path:
     - src/CommonModules/LockedOnlyModule/Module.bsl
   
   # Список полных квалифицированных имен объектов с ресурсами
   fullname:
     - Catalog.Products.Form.ItemForm.Form
     
# секция позволяет сделать исключения из правил блокировки
enable:
  subsystem:
    - EnabledSubsystem
  path:
    - src/Catalogs/Enabled

```

## Демо пример


Откройте 1C:EDT и импортируйте проект из этого репозитория [EditingDemoConfig](EditingDemoConfig) в воркспейс.

[Демо настройки здесь](EditingDemoConfig/.settings/editing.yml).

Откройте объекты метаданных чтобы исследовать функциональность блокировки и разрешения!

