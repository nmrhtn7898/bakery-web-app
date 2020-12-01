package com.bread.auth.base;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.Inheritance;

@Disabled
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public abstract class AbstractServiceTest {
}
