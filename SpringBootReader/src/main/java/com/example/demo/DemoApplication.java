package com.example.demo;

import com.example.demo.entity.Customer;
import com.example.demo.repository.CustomerRepository;
import com.monitorjbl.xlsx.StreamingReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class DemoApplication implements CommandLineRunner {

	private final CustomerRepository customerRepository;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		long startTimeRead = System.currentTimeMillis();
		log.info("-> Reading file");

		InputStream is = new FileInputStream(new File("../customers.xlsx"));
		Workbook workbook = StreamingReader.builder()
				.rowCacheSize(50000)    // number of rows to keep in memory (defaults to 10)
				.bufferSize(65536)     // buffer size to use when reading InputStream to file (defaults to 1024)
				.open(is);             // InputStream or File for XLSX file (required)

		List<Customer> customers = StreamSupport.stream(workbook.spliterator(), false)
				.flatMap( sheet -> StreamSupport.stream(sheet.spliterator(), false) )
				.skip(1)
				.map( con -> {
					Customer customer = new Customer();
					customer.setId((long) con.getCell(0).getNumericCellValue() );
					customer.setName(con.getCell(1).getStringCellValue());
					customer.setLastName(con.getCell(2).getStringCellValue());
					customer.setAddress(con.getCell(3).getStringCellValue());
					customer.setEmail(con.getCell(4).getStringCellValue());
					return customer;
				} ).collect(Collectors.toList());
		long endTimeRead = System.currentTimeMillis();
		log.info("-> Reading finished , time " + (endTimeRead - startTimeRead) + " ms" );

		long startTimeWrite = System.currentTimeMillis();
		customerRepository.saveAll(customers);
		long endTimeWrite = System.currentTimeMillis();
		log.info("-> Write finished, time " + (endTimeWrite - startTimeWrite) + " ms");
	}
}
