package com.ritik.assignment.services;


import com.ritik.assignment.entities.Contact;
import com.ritik.assignment.repositories.ContactRepository;
import com.ritik.assignment.request.IdentifyRequest;
import com.ritik.assignment.response.ContactResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.event.ContainerAdapter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    @Transactional
    public ContactResponse saveRequest(IdentifyRequest request) {

        List<Contact> existingContacts = contactRepository.findByEmailOrPhoneNumber(
                request.getEmail(),
                request.getPhoneNumber()
        );

        if (existingContacts.isEmpty()) {
            return createPrimaryContact(request);
        }

        Contact primaryContact = findPrimaryContact(existingContacts);

        if (hasNewInformation(request, existingContacts)) {
            Contact secondaryContact = createSecondaryContact(request, primaryContact.getId());
            contactRepository.save(secondaryContact);
        }

        List<Contact> secondaryContacts = contactRepository.findByLinkedId(primaryContact.getId());
        existingContacts.addAll(secondaryContacts);

        ContactResponse response = createResponse(primaryContact, existingContacts);

        return response;
    }

    private ContactResponse createResponse(Contact primaryContact, List<Contact> existingContacts) {
        Set<String> emails = new HashSet<>();
        Set<String> phoneNumbers = new HashSet<>();
        Set<Long> secondaryIds = new HashSet<>();


        if (primaryContact.getEmail() != null) {
            emails.add(primaryContact.getEmail());
        }

        if (primaryContact.getPhoneNumber() != null) {
            phoneNumbers.add(primaryContact.getPhoneNumber());
        }

        existingContacts.stream()
                .filter(c -> c.getLinkPrecedence().equals(Contact.LinkPrecedence.SECONDARY))
                .forEach(c -> {
                    emails.add(c.getEmail());
                    phoneNumbers.add(c.getPhoneNumber());
                    secondaryIds.add(c.getId());
                });

        ContactResponse response = new ContactResponse();
        response.setEmails(new ArrayList<>(emails));
        response.setPhoneNumbers(new ArrayList<>(phoneNumbers));
        response.setPrimaryContactId(primaryContact.getId());
        response.setSecondaryContactIds(new ArrayList<>(secondaryIds));

        return response;
    }

    private Contact createSecondaryContact(IdentifyRequest request, Long id) {
        Contact contact = new Contact();
        contact.setEmail(request.getEmail());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setLinkedId(id);
        contact.setLinkPrecedence(Contact.LinkPrecedence.SECONDARY);
        return contactRepository.save(contact);
    }

    private boolean hasNewInformation(IdentifyRequest request, List<Contact> existingContacts) {
        boolean hasNewEmail = request.getEmail() != null &&
                existingContacts.stream()
                        .noneMatch(c -> request.getEmail().equals(c.getEmail()));

        boolean hasNewPhoneNumber = request.getPhoneNumber() != null &&
                existingContacts.stream()
                        .noneMatch(c -> request.getPhoneNumber().equals(c.getPhoneNumber()));

        return hasNewPhoneNumber || hasNewEmail;
    }

    private Contact findPrimaryContact(List<Contact> existingContacts) {
        Optional<Contact> primary = existingContacts.stream()
                .filter(c -> c.getLinkPrecedence() == Contact.LinkPrecedence.PRIMARY)
                .min(Comparator.comparing(Contact::getCreatedAt));

        if (primary.isPresent()) {
            return primary.get();
        }

        Contact oldest = existingContacts.stream()
                .min(Comparator.comparing(Contact::getCreatedAt))
                .orElseThrow();

        existingContacts.stream()
                .filter(c -> !c.getId().equals(oldest.getId()))
                .forEach(c -> {
                    c.setLinkedId(oldest.getId());
                    c.setLinkPrecedence(Contact.LinkPrecedence.SECONDARY);
                    contactRepository.save(c);
                });

        oldest.setLinkPrecedence(Contact.LinkPrecedence.PRIMARY);
        oldest.setLinkedId(null);

        return contactRepository.save(oldest);

    }

    private ContactResponse createPrimaryContact(IdentifyRequest request) {
        Contact contact = new Contact();
        contact.setEmail(request.getEmail());
        contact.setPhoneNumber(request.getPhoneNumber());
        contact.setLinkPrecedence(Contact.LinkPrecedence.PRIMARY);
        contact = contactRepository.save(contact);

        ContactResponse response = new ContactResponse();
        response.setPrimaryContactId(contact.getId());
        response.setEmails(Collections.singletonList(contact.getEmail()));
        response.setPhoneNumbers(Collections.singletonList(contact.getPhoneNumber()));
        response.setSecondaryContactIds(Collections.emptyList());

        return response;
    }
}
