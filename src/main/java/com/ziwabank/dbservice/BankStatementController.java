package com.ziwabank.dbservice;

// BankStatementController.java
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/bank-statement")
public class BankStatementController {

    @PostMapping("/generate")
    public String generateBankStatement(@RequestBody @Validated Request request) {
        // Read data from CSV file based on the provided parameters

        List<String> bankStatement = readBankStatementFromCsv(request);

        // Dummy response for illustration purposes
        if(bankStatement.isEmpty()){
            return "No transaction found for the provided request";
        }

        return callOtherApi(request.getEmailAddress(),bankStatement);
    }
    private String callOtherApi(String email,List<String> bankStatement) {
        String apiUrl = "http://report-svc:5000/convert";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/plain");
        String param=email+"\n"+bankStatement.toString();
        // Create request entity with headers and data
        HttpEntity<String> requestEntity = new HttpEntity<>(param, headers);

        // Create RestTemplate instance
        RestTemplate restTemplate = new RestTemplate();

        // Make the HTTP POST request
        ResponseEntity<String> responseEntity = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, String.class);

        // Handle the response as needed
        String responseBody = responseEntity.getBody();
        System.out.println("Response: " + responseBody);
        return "Data sent to another API successfully";
    }
    private List<String> readBankStatementFromCsv(Request request) {
        List<String> bankStatement = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("bankstatement.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Assuming the CSV format is simple and each line represents a transaction
                // You may need to adapt this logic based on your actual CSV structure
                String[] transactionData = line.split(",");
                String transactionDate = transactionData[0];
                String transactionEmail = transactionData[1];
                // Check if the transaction matches the requested criteria
                if ( isTransactionWithinDateRange(transactionDate, request.getStartDate(), request.getEndDate())&&transactionEmail.equals(request.getEmailAddress())) {
                    bankStatement.add(line);
                }
            }
        } catch (IOException e) {
            // Handle the exception (e.g., log it or return an error response)
            e.printStackTrace();
        }

        return bankStatement;
    }

    private boolean isTransactionWithinDateRange(String transactionDate, LocalDate startDate, LocalDate endDate) {
        // Implement logic to check if the transaction date is within the specified range
        // You may need to parse dates and compare them based on your date format
        // This is just a basic example
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(transactionDate, formatter);
        return (date.isAfter(startDate)||date.isEqual(startDate)) && (date.isBefore(endDate)||date.isEqual(endDate));
    }
}

