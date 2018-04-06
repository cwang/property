@Grab('org.apache.pdfbox:pdfbox:2.0.4')

import java.awt.Rectangle

import org.apache.pdfbox.pdmodel.*
import org.apache.pdfbox.text.*

final String SUFFIX = '.pdf'

def source = new File(args[0])
def dest = new File(args[1])

def files = []

if (source.file && source.name.endsWith(SUFFIX)) {
	files += source
}
else if (source.directory) {
	source.eachFile(groovy.io.FileType.FILES) {
		if (it.name.endsWith(SUFFIX)) {
			files += it
		}
	}
}
else {
	System.exit(1)
}

final String REG = 'table'
final Rectangle RECT = new Rectangle(0, 250,1000, 1000)
final int COLS = 7
final String ESCAPE = ""
final String DELIMITER = "\t"
String line = null

dest.text = ''

files.each { f -> 
	println "parsing ${f} ..."
	try {
		def doc = PDDocument.load(f)
		def stripper = new PDFTextStripperByArea()
		stripper.sortByPosition = true
		stripper.addRegion(REG, RECT)
		stripper.extractRegions(doc.getPage(0))
		def sr = new StringReader(stripper.getTextForRegion(REG))
		while ((line = sr.readLine()) != null) {
//			if (line ==~ /^[0-9].+$/) {
//				break
//			}

			if (line.startsWith('Total')) {
				break
			}

			if (! line.startsWith('HM')) {
				continue
			}

			def org = line.split()
			int len = org.length
			def upd = new String[COLS]
			upd[0] = org[0]
			upd[1] = ESCAPE
			(1..(len - COLS)).each { i -> 
				upd[1] += org[i]
				upd[1] += ' '
			}
			upd[1] += ESCAPE
			(2..(COLS - 1)).each { i ->
				upd[i] = ESCAPE + org[len - COLS + i] + ESCAPE
			}
			dest << upd.join(DELIMITER)
			dest << "\n"
		}

	        println "... done ${f}"

	}
	catch (Exception e) {
		println "... something wrong with ${f}: ${e}"
	}
}


