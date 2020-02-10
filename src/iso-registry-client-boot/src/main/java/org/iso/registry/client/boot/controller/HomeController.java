package org.iso.registry.client.boot.controller;

import de.geoinfoffm.registry.core.model.iso19135.RE_Register;
import org.iso.registry.client.boot.dto.HomePage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HomeController {

//    @Resource
//    private RegisterRepository registerRepository;

    @RequestMapping(path = "/data", method = RequestMethod.GET)
    public HomePage getData() {
//        List<RE_Register> registers = registerRepository.findAll();
        return HomePage.builder()
//                .registers(registers)
                .version("1.2.3")
                .build();
    }
}
