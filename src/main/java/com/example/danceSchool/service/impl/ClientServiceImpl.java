package com.example.danceSchool.service.impl;

import com.example.danceSchool.dto.ClientDto;
import com.example.danceSchool.dto.GroupDto;
import com.example.danceSchool.dto.SheduleReport;
import com.example.danceSchool.dto.SheduleRowMapper;
import com.example.danceSchool.entity.Client;
import com.example.danceSchool.entity.Group;
import com.example.danceSchool.exception.ClientException;
import com.example.danceSchool.repository.ClientRepository;
import com.example.danceSchool.repository.GroupRepository;
import com.example.danceSchool.repository.RoleRepository;
import com.example.danceSchool.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    private final GroupRepository groupRepository;
    private final ConversionService conversionService;
    private final JdbcTemplate jdbcTemplate;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ClientServiceImpl(ClientRepository clientRepository, GroupRepository groupRepository, ConversionService conversionService, JdbcTemplate jdbcTemplate, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.groupRepository = groupRepository;
        this.conversionService = conversionService;
        this.jdbcTemplate = jdbcTemplate;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ClientDto findClientById(Long id) {
        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientException("Client is not found"));
        return conversionService.convert(client, ClientDto.class);
    }

    @Override
    public ClientDto createClient(ClientDto clientDto) {
        Client client = conversionService.convert(clientDto, Client.class);
        return conversionService.convert(clientRepository.save(client), ClientDto.class);
    }

    @Override
    public ClientDto updateClient(ClientDto clientDto, Long id) {
        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientException("Client is not found"));
        client.setFirstName(clientDto.getFirstName());
        client.setSecondName(clientDto.getSecondName());
        client.setLastName(clientDto.getLastName());
        client.setBirthday(clientDto.getBirthday());
        client.setSex(clientDto.getSex());
        client.setEmail(clientDto.getEmail());
        client.setPhoneNumber(clientDto.getPhoneNumber());
        client.setLogin(clientDto.getLogin());
        client.setPassword(clientDto.getPassword());
        GroupDto groupDto = clientDto.getGroup();
        if (Objects.nonNull(groupDto)) {
            Long groupId = groupDto.getId();
            Group group = Objects.isNull(groupId) ?
                    conversionService.convert(groupDto, Group.class) :
                    groupRepository.getOne(groupId);
            groupRepository.save(group);
            client.setGroup(group);
        }
        return conversionService.convert(clientRepository.save(client), ClientDto.class);
    }

    @Override
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id).orElseThrow(() -> new ClientException("Client is not found"));
        clientRepository.delete(client);
    }

    @Override
    public List<ClientDto> getAll() {
        List<Client> clients = clientRepository.findAll();
        return clients.stream().map(client -> conversionService.convert(client, ClientDto.class)).collect(Collectors.toList());
    }

    @Override
    public List<SheduleReport> getLessonsSortedByBegin() {
        List<SheduleReport> lessons = new ArrayList<>();
        String query = "SELECT lesson.id, `begin`, `end`, `teacher_id`, first_name, second_name, last_name, `length`, group_level, status, `name` FROM lesson left outer join `group` on lesson.group_id = `group`.id left outer join dance d on `group`.dance_id = d.id left outer join person on teacher_id=person.id WHERE `begin`>=CURRENT_DATE ORDER BY `begin`";
        lessons.addAll(jdbcTemplate.query(query, new SheduleRowMapper()));
        return lessons;
    }

    @Override
    public List<SheduleReport> getLessonsSortedByEnd() {
        List<SheduleReport> lessons = new ArrayList<>();
        String query = "SELECT lesson.id, `begin`, `end`, `teacher_id`, first_name, second_name, last_name, `length`, group_level, status, `name` FROM lesson left outer join `group` on lesson.group_id = `group`.id left outer join dance d on `group`.dance_id = d.id left outer join person on teacher_id=person.id WHERE `begin`>=CURRENT_DATE ORDER BY `end`";
        lessons.addAll(jdbcTemplate.query(query, new SheduleRowMapper()));
        return lessons;
    }

    @Override
    public List<SheduleReport> getLessonsSortedByLength() {
        List<SheduleReport> lessons = new ArrayList<>();
        String query = "SELECT lesson.id, `begin`, `end`, `teacher_id`, first_name, second_name, last_name, `length`, group_level, status, `name` FROM lesson left outer join `group` on lesson.group_id = `group`.id left outer join dance d on `group`.dance_id = d.id left outer join person on teacher_id=person.id WHERE `begin`>=CURRENT_DATE ORDER BY `length`";
        lessons.addAll(jdbcTemplate.query(query, new SheduleRowMapper()));
        return lessons;
    }

    @Override
    public List<SheduleReport> getLessonsSortedByType() {
        List<SheduleReport> lessons = new ArrayList<>();
        String query = "SELECT lesson.id, `begin`, `end`, `teacher_id`, first_name, second_name, last_name, `length`, group_level, status, `name` FROM lesson left outer join `group` on lesson.group_id = `group`.id left outer join dance d on `group`.dance_id = d.id left outer join person on teacher_id=person.id WHERE `begin`>=CURRENT_DATE ORDER BY `name`";
        lessons.addAll(jdbcTemplate.query(query, new SheduleRowMapper()));
        return lessons;
    }

    @Override
    public List<SheduleReport> getLessonsSortedByTeacher() {
        List<SheduleReport> lessons = new ArrayList<>();
        String query = "SELECT lesson.id, `begin`, `end`, `teacher_id`, first_name, second_name, last_name, `length`, group_level, status, `name` FROM lesson left outer join `group` on lesson.group_id = `group`.id left outer join dance d on `group`.dance_id = d.id left outer join person on teacher_id=person.id WHERE `begin`>=CURRENT_DATE ORDER BY `second_name`";
        lessons.addAll(jdbcTemplate.query(query, new SheduleRowMapper()));
        return lessons;
    }

    @Override
    public void joinGroup(String email, Long groupId) {
        Client client = clientRepository.findByEmail(email);
        Group group = groupRepository.getOne(groupId);
        client.setGroup(group);
        if (!group.getClients().contains(client)) {
            group.addClient(client);
        }
    }

    @Override
    public Client findByLogin(String login) {
        return clientRepository.findByLogin(login);
    }

    @Override
    public Client findByLoginAndPassword(String login, String password) {
        Client client = findByLogin(login);
        if (client != null) {
            if (passwordEncoder.matches(password, client.getPassword())) {
                return client;
            }
        }
        return null;
    }
}


