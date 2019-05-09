package commands.toys.Quote.Json;

import java.util.ArrayList;
import java.util.List;

public class QuoteJson
{
    public QuoteJson(String name, String user, String message, String date)
    {
        this.name = name;
        this.user = user;
        this.message = message;
        this.date = date;
    }

    public QuoteJson(String name, String user, String message, String attachmentUrl, String date)
    {
        this.name = name;
        this.user = user;
        this.message = message;
        this.date = date;
        this.attachmentUrl = attachmentUrl;
    }

    public String name;
    public String user;
    public String message;
    public String date;
    public String attachmentUrl;
}
