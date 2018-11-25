library(Matrix)

start_time <- Sys.time()

args = commandArgs(trailingOnly=TRUE)

#print(args)

srcFilePath = args[1]
destFilePath = args[5]
nonseasonalparam = as.numeric(strsplit(args[2],",")[[1]])
seasonalparam = as.numeric(strsplit(args[3],",")[[1]])
seasonality = as.numeric(strsplit(args[4],",")[[1]])
data = unlist(read.csv(file=srcFilePath , header=FALSE, sep=","))

start_time <- Sys.time()
seasonal_model = arima(data , order = nonseasonalparam ,  seasonal = list(order = seasonalparam , period = seasonality ), include.mean = FALSE, transform.pars = FALSE, method = c("CSS"))
end_time <- Sys.time()

sprintf("R ARIMA's execution time: %f", (end_time - start_time))

writeMM(as(seasonal_model$coef, "CsparseMatrix"), destFilePath)
