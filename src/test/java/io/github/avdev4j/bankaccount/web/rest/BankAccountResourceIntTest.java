package io.github.avdev4j.bankaccount.web.rest;

import io.github.avdev4j.bankaccount.BankAccountKataApp;
import io.github.avdev4j.bankaccount.domain.Account;
import io.github.avdev4j.bankaccount.domain.User;
import io.github.avdev4j.bankaccount.repository.AccountRepository;
import io.github.avdev4j.bankaccount.repository.UserRepository;
import io.github.avdev4j.bankaccount.service.AccountService;
import io.github.avdev4j.bankaccount.service.OperationService;
import io.github.avdev4j.bankaccount.web.rest.errors.ExceptionTranslator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = BankAccountKataApp.class)
public class BankAccountResourceIntTest {

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private OperationService operationService;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountService accountService;

    private MockMvc restOperationMockMvc;

    @Before
    public void setup() {
        BankAccountResource bankAccountResource = new BankAccountResource(accountService, operationService);

        this.restOperationMockMvc = MockMvcBuilders.standaloneSetup(bankAccountResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter)
            .build();
    }

    @Test
    @Transactional
    public void getById() throws Exception {
        User user = userRepository.findOneByLogin("user").get();

        Account account = new Account();
        account.setBalance(new BigDecimal("100"));
        account.setUser(user);

        accountRepository.save(account);

        restOperationMockMvc.perform(get("/api/bankaccounts/" + account.getId())
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balance").value(account.getBalance()));
    }

    @Test
    @Transactional
    public void getAllByUserId() throws Exception {
        User user = userRepository.findOneByLogin("user").get();

        Account account = new Account();
        account.setBalance(new BigDecimal("100"));
        account.setUser(user);

        Account account2 = new Account();
        account2.setBalance(new BigDecimal("200"));
        account2.setUser(user);

        accountRepository.save(account);

        restOperationMockMvc.perform(get("/api/bankaccounts/").param("userId", user.getId().toString())
            .contentType(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
