package com.example.quizprototype_phy_che_deepseek

import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import java.io.File

class QuizServer {
    private var server: Server? = null

    fun start(folderPath: String, port: Int = 8080) {
        // إذا كان الخادم يعمل على نفس المجلد، لا تفعل شيئاً
        stop()
        
        try {
            server = Server(port).apply {
                val context = ServletContextHandler(ServletContextHandler.SESSIONS)
                context.contextPath = "/"
                context.resourceBase = folderPath
                
                // إضافة DefaultServlet لإدارة ملفات iSpring (html, js, css, data)
                val holderDefault = ServletHolder("default", DefaultServlet::class.java)
                holderDefault.setInitParameter("dirAllowed", "true")
                holderDefault.setInitParameter("welcomeServlets", "true")
                holderDefault.setInitParameter("redirectWelcome", "true")
                
                context.addServlet(holderDefault, "/")
                handler = context
                start()
            }
            android.util.Log.d("QUIZ_SERVER", "Server started successfully on port $port serving: $folderPath")
        } catch (e: Exception) {
            android.util.Log.e("QUIZ_SERVER", "Failed to start server", e)
        }
    }

    fun stop() {
        try {
            if (server != null && (server!!.isStarted || server!!.isStarting)) {
                server?.stop()
                server = null
                android.util.Log.d("QUIZ_SERVER", "Server stopped")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
