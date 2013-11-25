package ca.licef.proeaf.vocabulary;

import licef.reflection.Invoker;
import licef.reflection.ThreadInvoker;

import javax.servlet.http.HttpServlet;

/**
 * Created by IntelliJ IDEA.
 * User: amiara
 * Date: 21-Mar-2012
 */
public class VocabularyServlet extends HttpServlet {

    public void init() {
        try {
            (new ThreadInvoker(new Invoker(Vocabulary.getInstance().getVocabularyManager(),
                "ca.licef.proeaf.vocabulary.VocabularyManager",
                    "initVocabularyModule", new Object[]{}))).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
