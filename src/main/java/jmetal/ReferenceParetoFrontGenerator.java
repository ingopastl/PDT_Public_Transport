package jmetal;

import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import org.uma.jmetal.util.fileoutput.SolutionListOutput;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.PointSolution;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ReferenceParetoFrontGenerator {
    public static void run(int totalProgress) throws IOException {
        String frontFileName = "results" + File.separatorChar + totalProgress + File.separatorChar + "FUN.tsv";
        String referenceSetFileName = "results" + File.separatorChar + totalProgress + File.separatorChar + "referenceParetoSet.ps";
        String referenceFrontFileName = "results" + File.separatorChar + totalProgress + File.separatorChar + "referenceParetoFront.pf";

        NonDominatedSolutionListArchive<PointSolution> nonDominatedSolutionArchive = new NonDominatedSolutionListArchive<>();

        Front front = new ArrayFront(frontFileName);
        List<PointSolution> solutionList = FrontUtils.convertFrontToSolutionList(front);

        for (PointSolution solution : solutionList) {
            nonDominatedSolutionArchive.add(solution);
        }

        new SolutionListOutput(nonDominatedSolutionArchive.getSolutionList()).printObjectivesToFile(referenceSetFileName);

        List<PointSolution> nonDominatedSolutions = nonDominatedSolutionArchive.getSolutionList();
        new SolutionListOutput(nonDominatedSolutions).printObjectivesToFile(referenceFrontFileName);
    }
}
