# Disable Editing plugin for 1C:EDT

> Russian description goes after English

> Русское описание идет после английского

Plugin for 1C:Enterprise Development Tools disables editing of 1C metadata objects in UI editors. Reach settings of blocked objects.

Historically developers used supplier's settings to enable editing of some metadata objects and the rest of objects keep disabled (aka supplied by Configuration vendor).

In new IDE 1C:EDT application develops more flexible and uses git for supplier-master branch workflow, initial supplier's settings now can be still used but give more tricks than more help. So most developers remove them from repository.

Developers want to protect themselves or their juniors from accidental editing some object that they didn't want to edit while exploring reach Configurations.

Disable Editing plugin developer set up some rules to make read-only mode in UI editors of 1C:EDT.


## Install


| Plugin Version | 1C:EDT version |
|----------------|----------------|
| 0.1.0 | 2020.1 - 2020.4 |
| 0.2.0 | 2020.5+ and newer... |


[![Drag to your running 1C:EDT* workspace. *Requires Eclipse Marketplace Client](https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png)](http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=5141319 "Drag to your running 1C:EDT* workspace. *Requires Eclipse Marketplace Client")


For manual install Select menu: `Help -> Install New Software...` past in p2-repository URL [https://marmyshev.gitlab.io/edt-editing/update](https://marmyshev.gitlab.io/edt-editing/update)

Select installable feature `Disable Editing plugin for 1C:EDT`


## Main features

Set up rules to **Disable** editing (read-only mode) in editors:

- specify subsystems which content objects are disabled to edit, hierarchy of subsystems is supported
- specify project relative full path or folders which content is disabled to edit
- specify full qualified names of 1C:Enterprise metadata objects (that stores in single file) that should be disabled in editors

Set up rules to **Enable** as exceptions from disable rules:

- by subsystem
- by project relative path to file or to folder
- by full qualified name


> Note! This plugin does not disables editing files via file system!

**Attantion!**  Disabled object will be skipped from project validation by 1C:EDT.


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

Плагин для 1C:Enterprise Development Tools блокирует редактирование объектов метаданных 1С в редакторах в интерфейсе. Гибкие настройки блокируемых объектов.

Исторически, разработчики использовали настройки поддержки поставщика чтобы разрешить редактирование некоторых объектов метаданных, а остальные объекты оставить заблокированными (типа на поддержке поставщика Конфигурации).

В новой IDE 1C:EDT  приложения разрабатываются гибче, в т.ч. используя Git для ветвления supplier-master, начальные настройки поставки можно при этом использовать, но добавляют сложности больше чем помощи. Поэтому большинство разработчиков  удаляют настройки из репозитория.

Разрабочики желают защитить себя или более молодых коллег от случайного редактирования некоторых объектов, которые бы не хотели редактировать в процессе изучения больших Конфигураций.

В Плагине запрета редактирования разработчик настраивает некоторые правила, чтобы включить режим Только-просмотр в редакторах интерфейса 1C:EDT.


## Установка


| Версия плагина | Версия 1C:EDT |
|----------------|---------------|
| 0.1.0 | 2020.1 - 2020.4 |
| 0.2.0 | 2020.5+ и новее... |


[![Перетяните это в *запущенный воркспейс EDT*. Требуется клиент Маркетплейс Эклипса](https://marketplace.eclipse.org/sites/all/themes/solstice/public/images/marketplace/btn-install.png)](http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=5141319 "Перетяните это в *запущенный воркспейс EDT*. Требуется клиент Маркетплейс Эклипса")


Для ручной установки Выбеите меню: `Help -> Install New Software...` вставьте адрес p2-репозитория [https://marmyshev.gitlab.io/edt-editing/update](https://marmyshev.gitlab.io/edt-editing/update)

Выберите фичу для установки `Disable Editing plugin for 1C:EDT`


## Основные возможности

Настройка правил блокировки ( **Disable** ) редактирования (режим Только-просмотр) в редакторах:

- Указание подсистем, состав объектов которых  заблокирован для редактирования, иерархия подсистем поддерживается
- Указание относительно проекта полного пути к файлу или к  каталогу, контент которых заблокирован для редактирования
- Указание полного квалифицированного имени метаданного 1С:Предприятия (хранящиеся в отдельных файлах), которые должны быть заблокированы в редакторах

Настройка правила исключения ( **Enable** )  для разрешения редактирования:

- по подсистеме
- по пути к файлу или каталогу относительно проекта
- по полному квалифицированному имени


> Примечание! Этот плагин не  блокирует редактирование файлов через файловую систему!

**Внимание!**  Заблокированные объекты будут исключены из проверки (валидации) по проекту в 1C:EDT.


##  Настройка правил по проекту


Разместите Ямл файл с настройками в каталоге вашего проектта: `YourProjectName/.settings/editing.yml` с содержанием:


```yaml
# секция блокирирует редактирование в UI
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
     
# секция добавляет исключения из правил блокировки
enable:
  subsystem:
    - EnabledSubsystem
  path:
    - src/Catalogs/Enabled

```

## Демо пример


Откройте 1C:EDT и импортируйте проект из этого репозитория [EditingDemoConfig](EditingDemoConfig) в воркспейс.

[Демо настройки здесь](EditingDemoConfig/.settings/editing.yml).

Откройте объекты метаданных, чтобы исследовать функциональность блокировки и разрешения!

