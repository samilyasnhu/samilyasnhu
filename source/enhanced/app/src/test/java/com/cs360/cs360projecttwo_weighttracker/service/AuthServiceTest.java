package com.cs360.cs360projecttwo_weighttracker.service;

import com.cs360.cs360projecttwo_weighttracker.data.WeightRepository;
import com.cs360.cs360projecttwo_weighttracker.domain.Result;
import com.cs360.cs360projecttwo_weighttracker.domain.WeightRecord;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AuthServiceTest {

    @Test
    public void loginRejectsMissingCredentialsBeforeRepositoryCall() {
        FakeRepository repository = new FakeRepository();
        AuthService service = new AuthService(repository);

        Result<Integer> result = service.logIn("", "");

        assertFalse(result.isSuccessful());
        assertEquals(0, repository.authenticationCalls);
    }

    @Test
    public void validLoginReturnsUserId() {
        FakeRepository repository = new FakeRepository();
        repository.authenticatedUserId = 42;
        AuthService service = new AuthService(repository);

        Result<Integer> result = service.logIn("samuel", "password123");

        assertTrue(result.isSuccessful());
        assertEquals(Integer.valueOf(42), result.getValue());
    }

    private static class FakeRepository implements WeightRepository {
        int authenticatedUserId = -1;
        int authenticationCalls = 0;

        @Override
        public boolean createUser(String username, String password) {
            return true;
        }

        @Override
        public int authenticateUser(String username, String password) {
            authenticationCalls++;
            return authenticatedUserId;
        }

        @Override
        public long saveWeightEntry(int userId, String date, double weight, Double goalWeight) {
            return 1;
        }

        @Override
        public boolean updateWeightEntry(int userId, int entryId, String date, double weight) {
            return true;
        }

        @Override
        public boolean deleteWeightEntry(int userId, int entryId) {
            return true;
        }

        @Override
        public List<WeightRecord> getWeightEntries(int userId) {
            return new ArrayList<>();
        }

        @Override
        public List<WeightRecord> getWeightEntriesInRange(
                int userId,
                String startDate,
                String endDate
        ) {
            return new ArrayList<>();
        }

        @Override
        public boolean setGoalWeight(int userId, double goalWeight) {
            return true;
        }

        @Override
        public Double getGoalWeight(int userId) {
            return null;
        }
    }
}
