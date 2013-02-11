import org.grails.plugin.easygrid.GridUtils
import org.grails.plugin.easygrid.grids.DataTablesGridService

enableCodeSave=false
grails.project.groupId = appName
grails.mime.file.extensions = true
grails.mime.use.accept.header = false
//export plugin
grails.mime.types = [html: ['text/html', 'application/xhtml+xml'],
        xml: ['text/xml', 'application/xml'],
        text: 'text-plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        pdf: 'application/pdf',
        rtf: 'application/rtf',
        excel: 'application/vnd.ms-excel',
        ods: 'application/vnd.oasis.opendocument.spreadsheet',
        all: '*/*',
        json: ['application/json', 'text/json'],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data'
]

grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']
grails.views.default.codec = "none"
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
grails.views.gsp.sitemesh.preprocess = true
grails.scaffolding.templates.domainSuffix = 'Instance'
grails.json.legacy.builder = false
grails.enable.native2ascii = true
grails.spring.bean.packages = []
grails.web.disable.multipart=false
grails.exceptionresolver.params.exclude = ['password']
grails.hibernate.cache.queries = false

environments {
	development {
		grails.logging.jul.usebridge = true
	}
	production {
		grails.logging.jul.usebridge = false
	}
}

log4j = {
	error 'org.codehaus.groovy.grails',
	      'org.springframework',
	      'org.hibernate',
	      'net.sf.ehcache.hibernate'
}

// Added by Easygrid:


def stdDateFormat =  'MM/dd/yyyy'
easygrid {

    //default values added to each defined grid  ( if they are not already set )
    defaults {

        defaultMaxRows = 10 // the max no of rows displayed in the grid

        //used for automatically generating label messages from the column name
        //this will be transformed into a SimpleTemplateEngine instance ( '#' will be replaced with '$') and the binding variables will be: labelPrefix , column, gridConfig
        labelFormat = '#{labelPrefix}.#{column.name}.label'

        //called before inline editing : transforms the parameters into the actual object to be stored
        beforeSave = { params -> params }

        gridImpl = 'jqgrid' // the default grid implementation

        // these are properties used by the jqgrid template
        enableFilter = true
        addNavGrid = true

        //default export settings for various formats
        export {
            exportService = org.grails.plugin.easygrid.EasygridExportService

            //this section provides default values for the different export formats
            // for more options check out the export plugin

            // csv settings
            csv {
                separator = ','
                quoteCharacter  = '"'
            }
            csv['header.enabled'] = true


            // excel settings
            excel['header.enabled'] = true
            //property that aggregates the widths defined per column
            excel['column.widths'] = { gridConfig ->
                def widths = []
                GridUtils.eachColumn(gridConfig, true) { column ->
                    widths.add(column?.export?.width ?: 0.2)
                }
                widths
            }

            // pdf settings
            pdf['header.enabled'] = true
            pdf['column.widths'] = { gridConfig ->
                def widths = []
                GridUtils.eachColumn(gridConfig, true) { column ->
                    widths.add(column?.export?.width ?: 0.2)
                }
                widths
            }
            pdf['border.color'] = java.awt.Color.BLACK
            pdf['pdf.orientation'] = 'landscape'


            // rtf settings
            rtf['header.enabled'] = true
            rtf {
            }

            // ods settings
            ods {
            }

            // xml settings
            xml['xml.root']= { gridConfig ->
                //defaults to the export title
                gridConfig.export.export_title
            }
            xml {
            }
        }

        // jqgrid default properties
        // check the jqgrid documentation
        jqgrid {
//            width = '"100%"'
//            height = 120
            // number of rows to display by default
//            rowNum = 10
        }

        // default security provider
        // spring security implementation
        // interprets the 'roles' property
        securityProvider = { grid, oper ->
            if (!grid.roles) {
                return true
            }
            def grantedRoles
            if (Map.isAssignableFrom(grid.roles.getClass())) {
                grantedRoles = grid.roles.findAll { op, role -> oper == op }.collect { op, role -> role }
            } else if (List.isAssignableFrom(grid.roles.getClass())) {
                grantedRoles = grid.roles
            } else {
                grantedRoles = [grid.roles]
            }
            SpringSecurityUtils.ifAllGranted(grantedRoles.inject('') { roles, role -> "${roles},${role}" })
        }

        //default autocomplete settings
        autocomplete {
            idProp = 'id'  // the name of the property of the id of the selected element (optionKey - in the replaced select tag)
            maxRows = 10 // the max no of elements to be displayed by the jquery autocomplete box
            template = '/templates/autocompleteRenderer' //the default autocomplete renderer
        }
    }

    // each grid has a "type" - which must be one of the datasources defined here
    dataSourceImplementations {
        //deprecated
        domain {
            // mandatory attribute: domainClass or initialCriteria
            dataSourceService = org.grails.plugin.easygrid.datasource.GormDatasourceService
            filters {
                //default search closures for different column types
                text = { filter -> ilike(filter.column.name, "%${filter.paramValue}%") }
                number = { filter -> eq(filter.column.name, filter.paramValue as int) }
                date = { filter -> eq(filter.column.name, new java.text.SimpleDateFormat(stdDateFormat).parse(filter.paramValue) ) }
            }
        }

        // renamed for consistency - todo  -rename everywhere
        gorm {
            // mandatory attribute: domainClass or initialCriteria
            dataSourceService = org.grails.plugin.easygrid.datasource.GormDatasourceService
            filters {
                //default search closures
                text = { filter -> ilike(filter.column.name, "%${filter.paramValue}%") }
                number = { filter -> eq(filter.column.name, filter.paramValue as int) }
                date = { filter -> eq(filter.column.name, new java.text.SimpleDateFormat(stdDateFormat).parse(filter.paramValue) ) }
            }
        }

        list {
            //mandatory attributes: context, attributeName
            dataSourceService = org.grails.plugin.easygrid.datasource.ListDatasourceService
            filters {
                //default search closures
                text = { filter, row -> row[filter.column.name].contains filter.paramValue }
                number = { filter, row -> row[filter.column.name] == filter.paramValue as int }
                date = { filter, row -> row[filter.column.name] == new java.text.SimpleDateFormat(stdDateFormat).parse(filter.paramValue)  }
            }
        }

        custom {
            // mandatory attributes: 'dataProvider', 'dataCount'
            dataSourceService = org.grails.plugin.easygrid.datasource.CustomDatasourceService
        }
    }

    // these are the actual UI grid implementations
    // will be specified in the grid config using the 'gridImpl' property
    gridImplementations {

        //grails classic implementation - for demo purposes
        classic {
            gridRenderer = '/templates/easygrid/classicGridRenderer'
            gridImplService = org.grails.plugin.easygrid.grids.ClassicGridService
            inlineEdit = false
            formats = [
                    (Date): { it.format(stdDateFormat) },
                    (Boolean): { it ? "Yes" : "No" }
            ]
        }

        //  jqgrid implementation
        jqgrid {
            gridRenderer = '/templates/easygrid/jqGridRenderer'          //  a gsp template that will be rendered
            gridImplService = org.grails.plugin.easygrid.grids.JqueryGridService  // the service class for this implementation
            inlineEdit = true    // specifies that this implementation allows inline Editing
            editRenderer = '/templates/easygrid/jqGridEditResponse'

            // there are 3 options to format the data
            // using the value closure in the column
            // using the named formatters ( defined below )
            // using the default type formats ( defined here ) - where you specify the type of data & the format closure
            formats = [
                    (Date): { it.format(stdDateFormat) },
                    (Calendar): { Calendar cal -> cal.format(stdDateFormat) },
                    (Boolean): { it ? "Yes" : "No" }
            ]
        }

        //  jquery datatables implementation
        dataTables {
            gridImplService = DataTablesGridService
            gridRenderer = '/templates/easygrid/dataTablesGridRenderer'
            inlineEdit = false
            formats = [
                    (Date): { it.format(stdDateFormat) },
                    (Boolean): { it ? "Yes" : "No" }
            ]
        }

        // google visualization implementation
        visualization {
            gridImplService = org.grails.plugin.easygrid.grids.VisualizationGridService
            gridRenderer = '/templates/easygrid/visualizationGridRenderer'
            inlineEdit = false
            formats = [
                    (Date): { def cal = com.ibm.icu.util.Calendar.getInstance(); cal.setTime(it); cal.setTimeZone(com.ibm.icu.util.TimeZone.getTimeZone("GMT")); cal }, //wtf?
            ]
        }

    }


    // section to define per column configurations
    columns {

        //default values for the columns
        defaults {
            enableFilter = true
            showInSelection = true
            jqgrid {
                editable = true
            }
            classic {
                sortable = true
            }
            visualization {
                search = false
                searchType = 'text'
                valueType = com.google.visualization.datasource.datatable.value.ValueType.TEXT
            }
            dataTables {
                width = "'100%'"
            }
            export {
                width = 25
            }
        }

        // predefined column types  (set of configurations)
        // used to avoid code duplication
        types {
            id {
                property = 'id'
                enableFilter = false

                jqgrid {
                    width = 40
                    fixed = true
                    search = false
                    editable = false
//                formatter = 'editFormatter'
                }
                visualization {
                    valueType = com.google.visualization.datasource.datatable.value.ValueType.NUMBER
                }
                export {
                    width = 10
                }

            }

            actions {
                value = { '' }
                jqgrid {
                    formatter = '"actions"'
                    editable = false
                    sortable = false
                    resizable = false
                    fixed = true
                    width = 60
                    search = false
                    formatoptions = '{"keys":true}'
                }
                export {
                    hidden = true
                }
            }

            version {
                property = 'version'
                jqgrid {
                    hidden = true
                }
                export {
                    hidden = true
                }
                visualization {
                    valueType = com.google.visualization.datasource.datatable.value.ValueType.NUMBER
                }
            }
        }
    }

    // here we define different formatters
    // these are closures  which are called before the data is displayed to format the cell data
    // these are specified in the column section using : formatName
    formats {
        stdDateFormatter = {
            it.format(stdDateFormat)
        }
        visualizationDateFormatter = {
            def cal = com.ibm.icu.util.Calendar.getInstance(); cal.setTime(it); cal.setTimeZone(java.util.TimeZone.getTimeZone("GMT")); cal
        }
        stdBoolFormatter = {
            it ? "Yes" : "No"
        }
    }
}


