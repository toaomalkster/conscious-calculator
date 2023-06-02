def modifyFile(srcFile, destFile, Closure c={println it;return it}) {
    StringBuffer ret=new StringBuffer()
    File src=new File(srcFile)
    File dest=new File(destFile)
    var newline = System.getProperty("line.separator")

    src.withReader{reader->
        reader.eachLine{
            def line=c(it)
            if(line != null) {
                ret.append(line)
                ret.append(newline)
            }
        }
    }
    dest.delete()
    dest.write(ret.toString())
}

modifyFile("test/text.md", "test/text.md");