package com.example.lab1.Controller;

import com.example.lab1.Validations.Results;
import com.example.lab1.logger.Logger;
import com.example.lab1.Repository;
import com.example.lab1.DataClass;
import com.example.lab1.Cache.cache;
import com.example.lab1.App;
import com.example.lab1.Results.Solution;
import com.example.lab1.Validations.InputValidation;
import org.apache.logging.log4j.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import com.example.lab1.Stats.Statistics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@RestController
public class MainController {

    private final AtomicLong counter = new AtomicLong();
    RequestCounterController NumberOfRequests = new RequestCounterController();
    Repository rep = new Repository();
    @GetMapping("/app")
    public Results Enter (
            @RequestParam(value = "year", required = true, defaultValue = "2022") int iYear,
            @RequestParam(value = "month", required = true, defaultValue = "4") int iMonth,
            @RequestParam(value = "date", required = true, defaultValue = "5") int iDate)
    {
        NumberOfRequests.IncremetNumber();
        App ThisDay = new App(iYear, iMonth, iDate);
        Logger.log(Level.INFO,  "Successfully getMapping");
        if(rep.isContain(ThisDay)) {
            var b = new Solution(ThisDay);
            b.calculateRoot();
            Logger.log(Level.INFO,  "This date is already in the cache");
            return rep.getParameters(ThisDay);
        }
        else {
            var b = new Solution(ThisDay);
            b.calculateRoot();
            Logger.log(Level.INFO,  "This date is added in the cache");
            return rep.addToMap(ThisDay,InputValidation.optionsValidation(counter.incrementAndGet(),ThisDay));
        }
    }


    @GetMapping("/cache")
    public ResponseEntity<String> printCache() {
        return new ResponseEntity<>(cache.getStaticStringCache(), HttpStatus.OK);
    }


    @PostMapping(value = "/post", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity alternativeCalculation(@RequestBody int[] array) {
        return new ResponseEntity<>(Statistics.post(array), HttpStatus.OK);
    }


    private static final String template = "\nDate: %d.%d.%d, Day: %s";

    @PostMapping("/stats")
    public ResponseEntity<?> EnterStream(@Valid @RequestBody List<DataClass> bodyList){
        Statistics st = new Statistics();
        List<String> res_output = new ArrayList<>();
        NumberOfRequests.IncremetNumber();
        int[] days = {0,0,0,0,0,0,0}; String popular = ""; int max = 0;
         //res_output.add(String.format(template, st.findJanuary(bodyList), st.findDecember(bodyList)));
        bodyList.forEach((element)->{
            res_output.add(String.format(template, element.getDate(), element.getMonth(), element.getYear(), st.Day(element)));
            if (st.Day(element) == "Monday") days[0]++;
            if (st.Day(element) == "Thursday") days[1]++;
            if (st.Day(element) == "Wednesday") days[2]++;
            if (st.Day(element) == "Thursday") days[3]++;
            if (st.Day(element) == "Friday") days[4]++;
            if (st.Day(element) == "Saturday") days[5]++;
            if (st.Day(element) == "Sunday") days[6]++;
        });
        max = 0;
        for (int i = 0; i<6; i++){
            if (days[i+1] > days[i]) max = i+1;
        }

        if (max == 0) popular = "Monday";
        if (max == 1) popular = "Thursday";
        if (max == 2) popular = "Wednesday";
        if (max == 3) popular = "Thursday";
        if (max == 4) popular = "Friday";
        if (max == 5) popular = "Saturday";
        if (max == 6) popular = "Sunday";

        long size = st.calcSize(bodyList);

        return new ResponseEntity<>("Size:" + size +"\n"+ res_output + "\nMost popular: " + popular, HttpStatus.OK);
    }

}
