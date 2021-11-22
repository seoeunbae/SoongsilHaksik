package ssu.haksik.haksik.dodam;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ssu.haksik.haksik.common.crawling.HaksikCrawling;
import ssu.haksik.haksik.common.response.Data;
import ssu.haksik.haksik.common.response.FoodResponse;

import java.io.IOException;

@RestController
@RequestMapping("/haksik/dodam")
public class DodamController {
    @PostMapping()
    public FoodResponse dodamHaksik(@RequestParam("time") String time) throws IOException {
        String url = "http://m.soongguri.com/m_req/m_menu.php?rcd=2&sdt=";
        String foods = HaksikCrawling.crawling(url,time);
        Data data = new Data(foods);
        FoodResponse foodResponse = new FoodResponse("2.0", data);
        return foodResponse;
    }
}
