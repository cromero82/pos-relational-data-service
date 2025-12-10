package com.infinitesoft.pos_relational_data_service.services.impl

import com.infinitesoft.pos_relational_data_service.entities.Ticket
import com.infinitesoft.pos_relational_data_service.repositories.TicketRepository
import spock.lang.Specification
import spock.lang.Subject

class TicketServiceImplSpec extends Specification {

    def ticketRepository = Mock(TicketRepository)
    @Subject
    def ticketService = new TicketServiceImpl(ticketRepository: ticketRepository)

    def "findById should return a ticket when a valid ID is provided"() {
        given: "A valid ticket ID"
        def ticketId = 1L
        def ticket = new Ticket(id: ticketId, nombre: "Test Ticket")
        ticketRepository.findById(ticketId) >> Optional.of(ticket)

        when: "The findById method is called"
        def result = ticketService.findById(ticketId)

        then: "The correct ticket is returned"
        result != null
        result.id == ticketId
        result.nombre == "Test Ticket"
    }

    def "findById should return null when an invalid ID is provided"() {
        given: "An invalid ticket ID"
        def ticketId = 999L
        ticketRepository.findById(ticketId) >> Optional.empty()

        when: "The findById method is called"
        def result = ticketService.findById(ticketId)

        then: "Null is returned"
        result == null
    }

    def "findById should return null when the ID is null"() {
        when: "The findById method is called with a null ID"
        def result = ticketService.findById(null)

        then: "Null is returned"
        result == null
    }
}
