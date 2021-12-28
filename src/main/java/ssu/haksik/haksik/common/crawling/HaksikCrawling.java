package ssu.haksik.haksik.common.crawling;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ssu.haksik.haksik.dodam.Dodam;
import ssu.haksik.haksik.dodam.DodamRepository;
import ssu.haksik.haksik.facultyLounge.FacultyLounge;
import ssu.haksik.haksik.facultyLounge.FacultyLoungeRepository;


@Component
@RequiredArgsConstructor
public class HaksikCrawling {

    private final DodamRepository dodamRepository;
    private final FacultyLoungeRepository facultyLoungeRepository;


    public static String crawling(String url, int eatingTime) throws IOException {

        LocalDateTime date = LocalDateTime.now();
        DayOfWeek day = date.getDayOfWeek();
        String today = day.toString();
        if(today.equals("SUNDAY") || today.equals("SATURDAY")){
            return "주말은 운영하지 않습니다.";
        }

        int time = eatingTime;
        String formatDate = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String todayUrl = url.concat(formatDate);
        Document document = Jsoup.connect(todayUrl).get();
        Elements lunchAndDinnerMenuListElements = document.getElementsByAttributeValue("class", "menu_list");
        Element menuListElementDividedByTime = lunchAndDinnerMenuListElements.get(time); // 점심 식단과 저녁식단을 구분
        Elements menuListElements = menuListElementDividedByTime.getElementsByTag("div"); // for문을 몇 번 수행해야 하는지 정하기 위해 총 Element가 몇 가지의 div로 이루어져 있는지 구한다.
        int size = menuListElements.size();

        boolean start = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < size; i++){
            Element menuListElement = menuListElements.get(i);

            if(menuListElement.getElementsContainingText("*").hasText() || menuListElement.getElementsContainingText("6.0").hasText() || menuListElement.getElementsContainingText("5.0").hasText()){
                start = true;
            }

            if(start){
                if (menuListElement.getElementsContainingText("알러지").hasText()) {
                    break;
                }

                if (menuListElement.hasText()) {
                    String foods = menuListElement.text();
                    if(foods.charAt(1)>=65 && foods.charAt(1)<=122){
                        continue;
                    }
                    if(foods.contains("-6.0") || foods.contains(" - 6.0") || foods.contains("-5.0") || foods.contains(" - 5.0")){
                        foods = foods.replace("-6.0","")
                                .replace(" - 6.0","")
                                .replace("-5.0","")
                                .replace(" - 5.0","");
                    }
                    sb.append(foods+"\n");
                }
            }
        }
        String foods = sb.toString();
        return foods;
    }


    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveDodamFoodMenu() throws IOException{
        String url = "http://m.soongguri.com/m_req/m_menu.php?rcd=2&sdt=";
        for (int eatingTime=0; eatingTime<2; eatingTime++) {
            String newDodamFoodMenu = crawling(url, eatingTime);
            Dodam dodamFoodMenuByTime = dodamRepository.findByEatingTime(eatingTime);
            if(dodamFoodMenuByTime == null){
                dodamRepository.save(new Dodam(newDodamFoodMenu, eatingTime));
            }else{
                dodamFoodMenuByTime.setFoods(newDodamFoodMenu);
                dodamRepository.save(dodamFoodMenuByTime);
            }
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 1 * * *")
    public void saveFacultyFoodMenu() throws IOException{
        String url = "http://m.soongguri.com/m_req/m_menu.php?rcd=7&sdt=";
        String newFacultyFoodMenu = crawling(url, 0);
        Optional<FacultyLounge> yesterdayFacultyFoodMenu= facultyLoungeRepository.findById(1L);
        if(yesterdayFacultyFoodMenu.isEmpty()){
            facultyLoungeRepository.save(new FacultyLounge(newFacultyFoodMenu));
            return;
        }else{
            FacultyLounge facultyFoodMenu = yesterdayFacultyFoodMenu.get();
            facultyFoodMenu.setFood(newFacultyFoodMenu);
            facultyLoungeRepository.save(facultyFoodMenu);
        }
    }
}
