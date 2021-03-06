package org.grails.samples

import org.grails.plugin.easygrid.Easygrid
import org.grails.plugin.easygrid.Filter

@Easygrid
class OverviewController {

    static grids = {
        ownersGrid {
            dataSourceType 'gorm'
            domainClass Owner
            columns {
                id {
                    type 'id'
                }
                firstName
                lastName
                address
                city
                telephone
                nrPets {
                    label 'owner.nrPets.label'
                    enableFilter false
                    value { owner ->
                        owner.pets.size()
                    }
                }
            }
        }

        petsGrid {
            dataSourceType 'gorm'
            domainClass Pet
            globalFilterClosure { params ->
                eq('owner.id', params.ownerId ? params.ownerId as long : -1l)
            }
            columns {
                name
                birthDate {
                    enableFilter false
                }
                pettype {
                    property 'type.name'
                    filterClosure{filter ->
                        type{
                            ilike('name', "%${filter.paramValue}%")
                        }
                    }
                }
                nrVisits {
                    label 'pet.nrVisits.label'
                    enableFilter false
                    value { Pet pet ->
                        pet.visits.size()
                    }
                }
            }
        }

        visitsGrid {
            dataSourceType 'gorm'
            domainClass Visit
            globalFilterClosure { params ->
                eq('pet.id', params.petId ? params.petId as long : -1l)
            }
            columns {
                vet {
                    name 'vet'
                    value { Visit visit ->
                        "${visit.vet.firstName} ${visit.vet.lastName}"
                    }
                    filterClosure { Filter filter ->
                        vet {
                            or {
                                ilike('firstName', "%${filter.paramValue}%")
                                ilike('lastName', "%${filter.paramValue}%")
                            }
                        }
                    }
                }
                description
                date{
                    enableFilter false
                }
            }
        }

        vetsGrid {
            dataSourceType 'gorm'
            domainClass Vet
            columns {
                firstName
                lastName
                specialities {
                    label 'vet.specialities.label'
                    name 'specialities'
                    value { Vet vet ->
                        vet.specialities.inject('') { val, item -> (val ? "${val}," : "") + item.name }
                    }
                    filterClosure { Filter filter ->
                        specialities {
                            ilike('name', "%${filter.paramValue}%")
                        }
                    }
                }
                nrOfVisits {
                    label 'vet.nrOfVisits.label'
                    name 'nrOfVisits'
                    value { Vet vet ->
                        Visit.countByVet(vet)
                    }
                    enableFilter false
                }
            }
            autocomplete {
                labelValue { val, params ->
                    "${val.firstName} ${val.lastName}"
                }
                textBoxFilterClosure { filter ->
                    or {
                        ilike('firstName', "%${filter.paramValue}%")
                        ilike('lastName', "%${filter.paramValue}%")
                    }
                }
            }
        }
    }

    def index() {}
}
