package me.rigelmc.rigelmcmod.httpd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.rigelmc.rigelmcmod.FreedomService;
import me.rigelmc.rigelmcmod.RigelMCMod;
import me.rigelmc.rigelmcmod.config.ConfigEntry;
import me.rigelmc.rigelmcmod.httpd.NanoHTTPD.HTTPSession;
import me.rigelmc.rigelmcmod.httpd.NanoHTTPD.Response;
import me.rigelmc.rigelmcmod.httpd.module.HTTPDModule;
import me.rigelmc.rigelmcmod.httpd.module.Module_dump;
import me.rigelmc.rigelmcmod.httpd.module.Module_file;
import me.rigelmc.rigelmcmod.httpd.module.Module_help;
import me.rigelmc.rigelmcmod.httpd.module.Module_list;
import me.rigelmc.rigelmcmod.httpd.module.Module_logs;
import me.rigelmc.rigelmcmod.httpd.module.Module_permbans;
import me.rigelmc.rigelmcmod.httpd.module.Module_players;
import me.rigelmc.rigelmcmod.httpd.module.Module_schematic;
import me.rigelmc.rigelmcmod.util.FLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class HTTPDaemon extends FreedomService
{

    public static String MIME_DEFAULT_BINARY = "application/octet-stream";
    private static final Pattern EXT_REGEX = Pattern.compile("\\.([^\\.\\s]+)$");
    //
    public int port;
    private HTTPD httpd;
    public Map<String, ModuleExecutable> modules = new HashMap<>();

    public HTTPDaemon(RigelMCMod plugin)
    {
        super(plugin);
    }

    @Override
    public void onStart()
    {
        if (!ConfigEntry.HTTPD_ENABLED.getBoolean())
        {
            return;
        }

        port = ConfigEntry.HTTPD_PORT.getInteger();
        httpd = new HTTPD(port);

        // Modules
        modules.clear();
        module("dump", Module_dump.class, true);
        module("file", Module_file.class, true);
        module("help", Module_help.class, false);
        module("list", Module_list.class, false);
        module("logs", Module_logs.class, true);
        module("permbans", Module_permbans.class, true);
        module("players", Module_players.class, false);
        module("schematic", Module_schematic.class, true);

        try
        {
            httpd.start();

            if (httpd.isAlive())
            {
                FLog.info("HTTP daemon started. Listening on port: " + httpd.getListeningPort());
            }
            else
            {
                FLog.info("Error starting the HTTP daemon.");
            }
        }
        catch (IOException ex)
        {
            FLog.severe(ex);
        }
    }

    @Override
    public void onStop()
    {
        if (!ConfigEntry.HTTPD_ENABLED.getBoolean())
        {
            return;
        }

        httpd.stop();

        FLog.info("HTTP daemon stopped.");
    }

    private void module(String name, Class<? extends HTTPDModule> clazz, boolean async)
    {
        modules.put(name, ModuleExecutable.forClass(plugin, clazz, async));
    }

    private class HTTPD extends NanoHTTPD
    {

        private HTTPD(int port)
        {
            super(port);
        }

        private HTTPD(String hostname, int port)
        {
            super(hostname, port);
        }

        @Override
        public Response serve(HTTPSession session)
        {
            final String[] args = StringUtils.split(session.getUri(), "/");

            ModuleExecutable mex = modules.get("file");
            if (args.length >= 1)
            {
                mex = modules.get(args[0].toLowerCase());
            }

            if (mex == null)
            {
                return new Response(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Error 404: Not Found - The requested resource was not found on this server.");
            }

            try
            {
                return mex.execute(session);
            }
            catch (Exception ex)
            {
                FLog.severe(ex);
                return new Response(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Error 500: Internal Server Error\r\n" + ex.getMessage() + "\r\n" + ExceptionUtils.getStackTrace(ex));
            }
        }
    }

    public static Response serveFileBasic(File file)
    {
        Response response = null;

        if (file != null && file.exists())
        {
            try
            {
                String mimetype = null;

                Matcher matcher = EXT_REGEX.matcher(file.getCanonicalPath());
                if (matcher.find())
                {
                    mimetype = Module_file.MIME_TYPES.get(matcher.group(1));
                }

                if (mimetype == null || mimetype.trim().isEmpty())
                {
                    mimetype = MIME_DEFAULT_BINARY;
                }

                response = new Response(Response.Status.OK, mimetype, new FileInputStream(file));
                response.addHeader("Content-Length", "" + file.length());
            }
            catch (IOException ex)
            {
                FLog.severe(ex);
            }
        }

        return response;
    }

}
